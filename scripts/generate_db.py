#!/usr/bin/env python3
"""
Generates the pre-packaged Room SQLite database from the NASA Exoplanet Archive CSV.

Usage:
    python3 scripts/generate_db.py

Output:
    exoplanet/src/main/assets/exoplanet_database.db

Run this script any time the CSV source data is updated. Commit the resulting
.db file. The app loads it directly via Room's createFromAsset() — no runtime
CSV parsing.
"""

import csv
import io
import os
import re
import sqlite3

CSV_PATH = os.path.join(os.path.dirname(__file__), "..", "data.csv")
OUT_PATH = os.path.join(os.path.dirname(__file__), "..", "exoplanet", "src", "main", "assets", "exoplanet_database.db")

# Room DB version — must match @Database(version = N) in ExoplanetDatabase.kt
DB_VERSION = 2


def split_csv_line(line: str) -> list[str]:
    """Replicates CsvParser.splitCsvLine() — handles quoted fields and <tag> blocks."""
    result = []
    sb = []
    in_quotes = False
    in_tag = False
    for ch in line:
        if ch == '<':
            in_tag = True
            sb.append(ch)
        elif ch == '>':
            in_tag = False
            sb.append(ch)
        elif ch == '"' and not in_tag:
            in_quotes = not in_quotes
        elif ch == ',' and not in_quotes and not in_tag:
            result.append(''.join(sb).strip())
            sb.clear()
        else:
            sb.append(ch)
    result.append(''.join(sb).strip())
    return result


def parse_csv(path: str):
    planets = []
    headers = None
    with open(path, encoding='utf-8') as f:
        for line in f:
            line = line.rstrip('\n')
            if line.startswith('#'):
                continue
            if headers is None:
                headers = [h.strip() for h in line.split(',')]
                continue
            values = split_csv_line(line)
            if len(values) < len(headers):
                continue
            row = dict(zip(headers, values))

            planet_name = row.get('pl_name', '').strip()
            host_name = row.get('hostname', '').strip()
            if not planet_name or not host_name:
                continue

            def i(key, default=0):
                v = row.get(key, '').strip()
                try:
                    return int(float(v)) if v else default
                except ValueError:
                    return default

            def f(key):
                v = row.get(key, '').strip()
                try:
                    return float(v) if v else None
                except ValueError:
                    return None

            def s(key):
                v = row.get(key, '').strip()
                return v if v else None

            planets.append({
                'planetName': planet_name,
                'hostName': host_name,
                'numStars': i('sy_snum', 1),
                'numPlanets': i('sy_pnum', 1),
                'discoveryMethod': row.get('discoverymethod', '').strip() or 'Unknown',
                'discoveryYear': i('disc_year', 0),
                'discoveryFacility': row.get('disc_facility', '').strip() or 'Unknown',
                'orbitalPeriodDays': f('pl_orbper'),
                'orbitSemiMajorAxisAu': f('pl_orbsmax'),
                'planetRadiusEarth': f('pl_rade'),
                'planetRadiusJupiter': f('pl_radj'),
                'planetMassEarth': f('pl_bmasse'),
                'planetMassJupiter': f('pl_bmassj'),
                'eccentricity': f('pl_orbeccen'),
                'insolationFlux': f('pl_insol'),
                'equilibriumTempK': f('pl_eqt'),
                'stellarEffectiveTempK': f('st_teff'),
                'stellarRadiusSolar': f('st_rad'),
                'stellarMassSolar': f('st_mass'),
                'stellarMetallicity': f('st_met'),
                'stellarSurfaceGravity': f('st_logg'),
                'spectralType': s('st_spectype'),
                'distanceParsec': f('sy_dist'),
                'ra': f('ra'),
                'dec': f('dec'),
                'isDefault': 1 if row.get('default_flag', '').strip() == '1' else 0,
            })
    return planets


def create_schema(conn: sqlite3.Connection):
    conn.executescript("""
        CREATE TABLE IF NOT EXISTS `star_systems` (
            `id`       INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `hostName` TEXT NOT NULL
        );
        CREATE INDEX IF NOT EXISTS `index_star_systems_hostName`
            ON `star_systems` (`hostName`);

        CREATE TABLE IF NOT EXISTS `exoplanets` (
            `id`                   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `systemId`             INTEGER NOT NULL,
            `planetName`           TEXT NOT NULL,
            `hostName`             TEXT NOT NULL,
            `numStars`             INTEGER NOT NULL,
            `numPlanets`           INTEGER NOT NULL,
            `discoveryMethod`      TEXT NOT NULL,
            `discoveryYear`        INTEGER NOT NULL,
            `discoveryFacility`    TEXT NOT NULL,
            `orbitalPeriodDays`    REAL,
            `orbitSemiMajorAxisAu` REAL,
            `planetRadiusEarth`    REAL,
            `planetRadiusJupiter`  REAL,
            `planetMassEarth`      REAL,
            `planetMassJupiter`    REAL,
            `eccentricity`         REAL,
            `insolationFlux`       REAL,
            `equilibriumTempK`     REAL,
            `stellarEffectiveTempK` REAL,
            `stellarRadiusSolar`   REAL,
            `stellarMassSolar`     REAL,
            `stellarMetallicity`   REAL,
            `stellarSurfaceGravity` REAL,
            `spectralType`         TEXT,
            `distanceParsec`       REAL,
            `ra`                   REAL,
            `dec`                  REAL,
            `isDefault`            INTEGER NOT NULL
        );
    """)


def main():
    print(f"Parsing CSV: {CSV_PATH}")
    planets = parse_csv(CSV_PATH)
    print(f"  {len(planets)} rows parsed")

    if os.path.exists(OUT_PATH):
        os.remove(OUT_PATH)

    conn = sqlite3.connect(OUT_PATH)
    create_schema(conn)

    # Insert star systems, collecting id per hostName
    unique_hosts = list(dict.fromkeys(p['hostName'] for p in planets))  # preserve order, dedupe
    host_to_id = {}
    conn.executemany(
        "INSERT INTO `star_systems` (`hostName`) VALUES (?)",
        [(h,) for h in unique_hosts]
    )
    for row in conn.execute("SELECT `id`, `hostName` FROM `star_systems`"):
        host_to_id[row[1]] = row[0]
    print(f"  {len(host_to_id)} star systems inserted")

    # Insert planets in chunks
    chunk_size = 500
    total = 0
    chunk = []
    for p in planets:
        chunk.append((
            host_to_id[p['hostName']],
            p['planetName'], p['hostName'],
            p['numStars'], p['numPlanets'],
            p['discoveryMethod'], p['discoveryYear'], p['discoveryFacility'],
            p['orbitalPeriodDays'], p['orbitSemiMajorAxisAu'],
            p['planetRadiusEarth'], p['planetRadiusJupiter'],
            p['planetMassEarth'], p['planetMassJupiter'],
            p['eccentricity'], p['insolationFlux'], p['equilibriumTempK'],
            p['stellarEffectiveTempK'], p['stellarRadiusSolar'], p['stellarMassSolar'],
            p['stellarMetallicity'], p['stellarSurfaceGravity'],
            p['spectralType'], p['distanceParsec'], p['ra'], p['dec'],
            p['isDefault'],
        ))
        if len(chunk) >= chunk_size:
            conn.executemany("""
                INSERT INTO `exoplanets` (
                    `systemId`, `planetName`, `hostName`,
                    `numStars`, `numPlanets`, `discoveryMethod`, `discoveryYear`, `discoveryFacility`,
                    `orbitalPeriodDays`, `orbitSemiMajorAxisAu`,
                    `planetRadiusEarth`, `planetRadiusJupiter`,
                    `planetMassEarth`, `planetMassJupiter`,
                    `eccentricity`, `insolationFlux`, `equilibriumTempK`,
                    `stellarEffectiveTempK`, `stellarRadiusSolar`, `stellarMassSolar`,
                    `stellarMetallicity`, `stellarSurfaceGravity`,
                    `spectralType`, `distanceParsec`, `ra`, `dec`, `isDefault`
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """, chunk)
            total += len(chunk)
            chunk.clear()
    if chunk:
        conn.executemany("""
            INSERT INTO `exoplanets` (
                `systemId`, `planetName`, `hostName`,
                `numStars`, `numPlanets`, `discoveryMethod`, `discoveryYear`, `discoveryFacility`,
                `orbitalPeriodDays`, `orbitSemiMajorAxisAu`,
                `planetRadiusEarth`, `planetRadiusJupiter`,
                `planetMassEarth`, `planetMassJupiter`,
                `eccentricity`, `insolationFlux`, `equilibriumTempK`,
                `stellarEffectiveTempK`, `stellarRadiusSolar`, `stellarMassSolar`,
                `stellarMetallicity`, `stellarSurfaceGravity`,
                `spectralType`, `distanceParsec`, `ra`, `dec`, `isDefault`
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """, chunk)
        total += len(chunk)

    conn.execute(f"PRAGMA user_version = {DB_VERSION}")
    conn.commit()
    conn.close()
    print(f"  {total} planets inserted")

    size_mb = os.path.getsize(OUT_PATH) / (1024 * 1024)
    print(f"Done → {OUT_PATH}  ({size_mb:.1f} MB)")


if __name__ == '__main__':
    main()

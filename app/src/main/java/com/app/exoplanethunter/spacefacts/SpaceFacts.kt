package com.app.exoplanethunter.spacefacts

/**
 * Local catalog of curated space / exoplanet facts and theories.
 *
 * Currently ~25 entries; structured so more can be appended toward 100 simply by adding
 * to [all] with the next sequential [SpaceFact.id]. Keep entries factual and on-topic
 * (space, exoplanets, astrobiology, SETI). Each [SpaceFact.detail] is ~100 words.
 */
object SpaceFacts {

    val all: List<SpaceFact> = listOf(
        SpaceFact(
            id = 1,
            title = "The Dark Forest Theory",
            shortDescription = "Why a universe full of life might still be eerily silent.",
            detail = "The Dark Forest hypothesis offers one answer to the Fermi paradox: the cosmos " +
                "may be teeming with civilizations that all stay silent out of fear. Because any " +
                "unknown civilization could be a threat, and because technology can advance " +
                "explosively, the safest survival strategy is to hide rather than broadcast. In " +
                "this view, every civilization is a hunter in a dark forest, and announcing your " +
                "position invites destruction. Popularized by Liu Cixin's science-fiction novel, " +
                "it reframes cosmic silence not as absence of life but as a deliberate, universal " +
                "act of caution among intelligent species.",
            sourceUrl = "https://en.wikipedia.org/wiki/Dark_forest_hypothesis"
        ),
        SpaceFact(
            id = 2,
            title = "Dyson Spheres",
            shortDescription = "A megastructure that could harvest an entire star's energy.",
            detail = "A Dyson sphere is a hypothetical megastructure that an advanced civilization " +
                "might build to capture a large fraction of a star's total energy output. Proposed " +
                "by physicist Freeman Dyson in 1960, the realistic version is not a solid shell but " +
                "a vast swarm of orbiting collectors (a Dyson swarm). Such a structure would absorb " +
                "starlight and re-radiate waste heat as infrared, so astronomers actually search for " +
                "Dyson spheres by looking for stars with unusual infrared excess. They serve as a " +
                "benchmark for Type II civilizations on the Kardashev scale, which harness the energy " +
                "of their entire host star.",
            sourceUrl = "https://en.wikipedia.org/wiki/Dyson_sphere"
        ),
        SpaceFact(
            id = 3,
            title = "The Three-Body Problem",
            shortDescription = "Why three orbiting masses defy a clean mathematical solution.",
            detail = "The three-body problem asks how three massive objects move under their mutual " +
                "gravity. While two bodies trace neat, predictable ellipses, adding a third makes the " +
                "system chaotic: there is no general closed-form solution, and tiny changes in starting " +
                "conditions lead to wildly different futures. Astronomers rely on numerical simulation " +
                "rather than exact equations. The problem matters for real exoplanets in multi-star " +
                "systems, whose orbits can be unstable or surprising. Special 'restricted' cases and " +
                "stable configurations like Lagrange points do have solutions, but the general problem " +
                "remains a classic example of deterministic chaos in physics.",
            sourceUrl = "https://en.wikipedia.org/wiki/Three-body_problem"
        ),
        SpaceFact(
            id = 4,
            title = "The Fermi Paradox",
            shortDescription = "If the galaxy should be full of life, where is everybody?",
            detail = "The Fermi paradox is the contradiction between high estimates for the likelihood " +
                "of extraterrestrial civilizations and the total lack of evidence for them. The galaxy " +
                "is billions of years old, and even slow interstellar travel could colonize it in a few " +
                "million years, so it 'should' show signs of life. Yet we observe silence. Proposed " +
                "resolutions range from life being extremely rare, to civilizations self-destructing, to " +
                "them deliberately hiding or simply being too far away to detect. Named after physicist " +
                "Enrico Fermi, the paradox frames much of the modern search for extraterrestrial " +
                "intelligence.",
            sourceUrl = "https://en.wikipedia.org/wiki/Fermi_paradox"
        ),
        SpaceFact(
            id = 5,
            title = "The Drake Equation",
            shortDescription = "A formula for estimating how many alien civilizations exist.",
            detail = "The Drake equation, written by astronomer Frank Drake in 1961, estimates the " +
                "number of communicating civilizations in our galaxy by multiplying several factors: " +
                "the rate of star formation, the fraction of stars with planets, the number of " +
                "habitable planets per system, how often life and then intelligence arise, the fraction " +
                "that develop detectable technology, and how long such civilizations last. Many terms " +
                "are still poorly known, so the equation yields answers ranging from one to millions. " +
                "Its real value is organizing our ignorance: it shows exactly which questions exoplanet " +
                "science and astrobiology need to answer.",
            sourceUrl = "https://en.wikipedia.org/wiki/Drake_equation"
        ),
        SpaceFact(
            id = 6,
            title = "The Kardashev Scale",
            shortDescription = "Ranking civilizations by how much energy they can command.",
            detail = "Proposed by Soviet astronomer Nikolai Kardashev in 1964, the Kardashev scale " +
                "classifies civilizations by their energy use. A Type I civilization harnesses all the " +
                "energy available on its home planet; a Type II commands the entire output of its star " +
                "(for example via a Dyson sphere); and a Type III controls the energy of an entire " +
                "galaxy. By these measures, humanity is not yet even Type I, sitting around 0.7. The " +
                "scale is a useful tool in SETI because energy use tends to produce detectable " +
                "signatures, like waste heat, that we might spot across interstellar distances.",
            sourceUrl = "https://en.wikipedia.org/wiki/Kardashev_scale"
        ),
        SpaceFact(
            id = 7,
            title = "The Habitable Zone",
            shortDescription = "The orbital 'Goldilocks' band where liquid water can exist.",
            detail = "A star's habitable zone, often called the Goldilocks zone, is the range of orbital " +
                "distances where a planet's surface temperature could allow liquid water — not too hot, " +
                "not too cold. Its location depends on the star: cool red dwarfs have habitable zones " +
                "hugging close in, while bright stars push them far out. Sitting in the zone is no " +
                "guarantee of life, since atmosphere, pressure, and greenhouse effects matter enormously " +
                "(Venus sits near the inner edge yet is hellish). Still, the habitable zone is the first " +
                "filter astronomers use when hunting for potentially life-bearing exoplanets.",
            sourceUrl = "https://en.wikipedia.org/wiki/Circumstellar_habitable_zone"
        ),
        SpaceFact(
            id = 8,
            title = "The Transit Method",
            shortDescription = "Finding planets by the tiny shadows they cast on their stars.",
            detail = "The transit method detects exoplanets by measuring the slight dimming of a star " +
                "when a planet passes in front of it. The dip is tiny — Earth crossing the Sun would " +
                "block about 0.008% of its light — but precise enough to reveal the planet's size and " +
                "orbital period, and repeated transits confirm it. NASA's Kepler and TESS missions used " +
                "this technique to discover thousands of worlds. As a bonus, starlight filtering through " +
                "a planet's atmosphere during transit carries chemical fingerprints, letting telescopes " +
                "like JWST probe distant atmospheres. It is the most productive exoplanet-detection " +
                "method to date.",
            sourceUrl = "https://en.wikipedia.org/wiki/Transit_(astronomy)"
        ),
        SpaceFact(
            id = 9,
            title = "The Radial Velocity Method",
            shortDescription = "Detecting planets by the wobble they induce in their star.",
            detail = "The radial velocity method finds exoplanets by measuring the tiny back-and-forth " +
                "wobble a planet's gravity induces in its host star. As the star moves toward and away " +
                "from us, its light shifts slightly blue then red, like a cosmic Doppler effect. " +
                "Measuring this shift reveals the planet's minimum mass and orbital period. This was the " +
                "technique behind 51 Pegasi b in 1995, the first planet found around a Sun-like star, " +
                "which earned a Nobel Prize. Radial velocity favors massive planets in close orbits and " +
                "nicely complements the transit method, together pinning down both a planet's mass and " +
                "size.",
            sourceUrl = "https://en.wikipedia.org/wiki/Doppler_spectroscopy"
        ),
        SpaceFact(
            id = 10,
            title = "The TRAPPIST-1 System",
            shortDescription = "Seven Earth-sized worlds orbiting one tiny, cool star.",
            detail = "TRAPPIST-1 is a small, cool red dwarf about 40 light-years away that hosts seven " +
                "known Earth-sized planets — the largest batch of terrestrial worlds found around a " +
                "single star. Several orbit within the habitable zone where liquid water could exist. " +
                "The planets are packed so tightly that their orbits last only days, and from one " +
                "planet's surface the neighboring worlds would loom larger than our Moon. Their orbits " +
                "form a delicate resonant chain, ticking in near-perfect ratios. TRAPPIST-1 is a prime " +
                "target for the James Webb Space Telescope, which is studying whether any of these " +
                "worlds hold atmospheres.",
            sourceUrl = "https://en.wikipedia.org/wiki/TRAPPIST-1"
        ),
        SpaceFact(
            id = 11,
            title = "Hot Jupiters",
            shortDescription = "Giant planets that orbit scorchingly close to their stars.",
            detail = "Hot Jupiters are gas giants similar in mass to Jupiter but orbiting astonishingly " +
                "close to their stars, often completing an orbit in just a few days. Their surfaces can " +
                "exceed 1,000°C, and the nearest face is blasted by relentless radiation. Their " +
                "discovery shocked astronomers, because giant planets were expected to form far from " +
                "their stars, not roast beside them. The leading explanation is migration: these worlds " +
                "form in the cold outer disk and then spiral inward. Because they are large and transit " +
                "frequently, hot Jupiters were among the easiest exoplanets to detect and remain " +
                "valuable laboratories for atmospheric science.",
            sourceUrl = "https://en.wikipedia.org/wiki/Hot_Jupiter"
        ),
        SpaceFact(
            id = 12,
            title = "Super-Earths",
            shortDescription = "Rocky worlds bigger than Earth but smaller than Neptune.",
            detail = "A super-Earth is a planet with a mass greater than Earth's but well below that of " +
                "ice giants like Neptune, roughly one to ten Earth masses. The term refers only to size, " +
                "not habitability — a super-Earth could be a rocky world, a water world, or wrapped in a " +
                "thick atmosphere. They appear to be among the most common types of planets in the " +
                "galaxy, even though our own Solar System has none. Their stronger gravity and possible " +
                "long-lived geology make them intriguing targets in the search for life, and many orbit " +
                "within their stars' habitable zones.",
            sourceUrl = "https://en.wikipedia.org/wiki/Super-Earth"
        ),
        SpaceFact(
            id = 13,
            title = "Tidal Locking",
            shortDescription = "When a planet always shows the same face to its star.",
            detail = "Tidal locking happens when a body's rotation slows until it always shows the same " +
                "face to the object it orbits — just as the Moon always shows one side to Earth. Many " +
                "exoplanets around cool red dwarfs are likely tidally locked, because they orbit so " +
                "close that tidal forces are strong. Such a world would have a permanent dayside baked " +
                "in starlight and a permanent night side in frozen darkness, with a twilight ring " +
                "between them that might be the most temperate place to live. Whether atmospheres can " +
                "redistribute heat enough for habitability is an active research question.",
            sourceUrl = "https://en.wikipedia.org/wiki/Tidal_locking"
        ),
        SpaceFact(
            id = 14,
            title = "Rogue Planets",
            shortDescription = "Starless worlds drifting alone through interstellar space.",
            detail = "Rogue planets are worlds that do not orbit any star, instead wandering freely " +
                "through the galaxy. Some were ejected from their birth systems by gravitational " +
                "encounters; others may have formed alone from collapsing gas clouds. Detected mainly " +
                "through gravitational microlensing, they could number in the billions across the Milky " +
                "Way — possibly outnumbering ordinary stars. Without a sun, their surfaces should be " +
                "frigid, yet internal heat or a thick hydrogen atmosphere might keep subsurface oceans " +
                "liquid, raising the strange possibility of life on a planet adrift in eternal night. " +
                "They are among the most mysterious objects in modern astronomy.",
            sourceUrl = "https://en.wikipedia.org/wiki/Rogue_planet"
        ),
        SpaceFact(
            id = 15,
            title = "Proxima Centauri b",
            shortDescription = "The closest known exoplanet to our Solar System.",
            detail = "Proxima Centauri b is an exoplanet orbiting Proxima Centauri, the nearest star to " +
                "the Sun at just 4.2 light-years away. Slightly more massive than Earth, it sits within " +
                "its star's habitable zone, where liquid water could in principle exist. But Proxima is " +
                "a red dwarf prone to powerful flares, which may strip away any atmosphere, and the " +
                "planet is probably tidally locked. Despite these challenges, its closeness makes it the " +
                "most tempting target for future interstellar study, including proposed missions like " +
                "Breakthrough Starshot that envision sending tiny probes there within a human lifetime.",
            sourceUrl = "https://en.wikipedia.org/wiki/Proxima_Centauri_b"
        ),
        SpaceFact(
            id = 16,
            title = "Pulsar Planets",
            shortDescription = "The first exoplanets ever found orbit a dead star.",
            detail = "The first confirmed exoplanets were not found around a normal star at all, but " +
                "orbiting a pulsar — the rapidly spinning, ultra-dense remnant of an exploded star. In " +
                "1992, astronomers detected planets around PSR B1257+12 by measuring tiny, regular " +
                "variations in the pulsar's precise radio beats. These worlds endure intense radiation " +
                "and likely formed from debris left after the supernova. Their discovery proved that " +
                "planets exist beyond our Solar System years before the first planet around a Sun-like " +
                "star was found, and showed that planetary systems can arise in some of the most hostile " +
                "environments imaginable.",
            sourceUrl = "https://en.wikipedia.org/wiki/PSR_B1257%2B12"
        ),
        SpaceFact(
            id = 17,
            title = "Gravitational Microlensing",
            shortDescription = "Using warped starlight as a natural planet-finding lens.",
            detail = "Gravitational microlensing detects planets by exploiting Einstein's relativity: a " +
                "massive object passing in front of a distant star bends and magnifies its light like a " +
                "lens. If the foreground star hosts a planet, the planet adds a brief extra spike to the " +
                "brightening. This method is uniquely sensitive to small planets far from their stars, " +
                "and even to starless rogue planets, complementing transit and radial-velocity surveys. " +
                "Its drawback is that each alignment is a one-time event that never repeats, so the " +
                "planets cannot be re-observed. Upcoming missions like the Nancy Grace Roman Space " +
                "Telescope will use it extensively.",
            sourceUrl = "https://en.wikipedia.org/wiki/Gravitational_microlensing"
        ),
        SpaceFact(
            id = 18,
            title = "Exomoons",
            shortDescription = "Moons orbiting planets in other star systems.",
            detail = "Exomoons are moons that orbit exoplanets, just as our Solar System's planets host " +
                "dozens of moons. None have been definitively confirmed yet, because they are far " +
                "smaller and harder to detect than planets, though several promising candidates exist. " +
                "Exomoons are exciting for astrobiology: a large moon orbiting a giant planet in the " +
                "habitable zone could itself be a haven for life, warmed by both starlight and tidal " +
                "heating, much as Jupiter's moon Europa hides a subsurface ocean. Detecting them " +
                "requires extraordinarily precise measurements of how a planet's transits subtly shift " +
                "in timing and shape.",
            sourceUrl = "https://en.wikipedia.org/wiki/Exomoon"
        ),
        SpaceFact(
            id = 19,
            title = "Biosignatures",
            shortDescription = "The chemical fingerprints that could betray alien life.",
            detail = "A biosignature is a substance or pattern that provides scientific evidence of life, " +
                "past or present. On exoplanets, astronomers hunt for atmospheric gases that life " +
                "produces — oxygen, methane, or especially combinations that should not coexist without " +
                "something constantly replenishing them. By splitting the starlight that passes through " +
                "a planet's atmosphere during transit, telescopes like JWST can read these chemical " +
                "fingerprints. The challenge is ruling out non-living explanations, since geology and " +
                "chemistry can mimic biology. A convincing detection would likely require multiple " +
                "gases in disequilibrium, making the search for biosignatures one of the most careful " +
                "pursuits in science.",
            sourceUrl = "https://en.wikipedia.org/wiki/Biosignature"
        ),
        SpaceFact(
            id = 20,
            title = "The Great Filter",
            shortDescription = "A barrier that may stop life from becoming a galactic civilization.",
            detail = "The Great Filter is a proposed answer to the Fermi paradox: somewhere along the " +
                "path from lifeless chemistry to a galaxy-spanning civilization lies at least one " +
                "extremely improbable step — a filter that almost nothing gets past. It could lie " +
                "behind us, meaning the origin of life or of intelligence is staggeringly rare and we " +
                "are exceptionally lucky. Or it could lie ahead, meaning civilizations tend to destroy " +
                "themselves before spreading. Unsettlingly, finding abundant simple alien life might be " +
                "bad news, suggesting the hardest filter still awaits us. The idea sharpens why cosmic " +
                "silence is so profound.",
            sourceUrl = "https://en.wikipedia.org/wiki/Great_Filter"
        ),
        SpaceFact(
            id = 21,
            title = "Panspermia",
            shortDescription = "The idea that life spreads between worlds aboard space rocks.",
            detail = "Panspermia is the hypothesis that life, or its chemical building blocks, can travel " +
                "between planets and even star systems, carried by comets, asteroids, and the debris " +
                "blasted off worlds by impacts. Hardy microbes such as tardigrades and certain bacteria " +
                "can survive vacuum, radiation, and extreme cold, lending the idea some plausibility " +
                "within a solar system. Panspermia does not explain how life originally began; it only " +
                "shifts the location of that origin. If true, it raises the possibility that life on " +
                "Earth and any life elsewhere in our system could share a common, wandering ancestry.",
            sourceUrl = "https://en.wikipedia.org/wiki/Panspermia"
        ),
        SpaceFact(
            id = 22,
            title = "Red Dwarf Habitability",
            shortDescription = "Could the galaxy's most common stars host life?",
            detail = "Red dwarfs are by far the most common stars in the galaxy, small and cool but " +
                "extraordinarily long-lived, burning steadily for trillions of years. That longevity " +
                "could give life ample time to arise. But habitability around them is debated: their " +
                "habitable zones lie so close that planets are likely tidally locked, and young red " +
                "dwarfs unleash violent flares and radiation that may strip planetary atmospheres. " +
                "Because they are so numerous and their small size makes orbiting planets easy to " +
                "detect, red dwarfs are central to the exoplanet search — and worlds like those of " +
                "TRAPPIST-1 are key test cases.",
            sourceUrl = "https://en.wikipedia.org/wiki/Habitability_of_red_dwarf_systems"
        ),
        SpaceFact(
            id = 23,
            title = "The Wow! Signal",
            shortDescription = "A 72-second radio burst that has never been explained.",
            detail = "On August 15, 1977, Ohio State University's Big Ear radio telescope recorded a " +
                "strong, narrow-band radio signal lasting 72 seconds, coming from the direction of the " +
                "constellation Sagittarius. The astronomer reviewing the printout circled the reading " +
                "and wrote 'Wow!' beside it, giving the signal its name. Its frequency was close to a " +
                "value many thought a deliberate transmitter might use, and it matched what an " +
                "extraterrestrial beacon could look like. Despite decades of follow-up, it has never " +
                "been detected again, and no natural source has been confirmed. It remains the most " +
                "tantalizing single event in SETI history.",
            sourceUrl = "https://en.wikipedia.org/wiki/Wow!_signal"
        ),
        SpaceFact(
            id = 24,
            title = "Kepler-452b",
            shortDescription = "An 'Earth cousin' orbiting a Sun-like star.",
            detail = "Announced in 2015, Kepler-452b was hailed as one of the most Earth-like planets " +
                "found at the time, orbiting a Sun-like star in its habitable zone with a year of about " +
                "385 days. Roughly 60% wider than Earth, it likely qualifies as a super-Earth and may be " +
                "rocky, though its mass is uncertain. Its star is older than the Sun, offering a glimpse " +
                "of what Earth's future could resemble as our own star brightens. Located some 1,400 " +
                "light-years away, Kepler-452b is far too distant to study in detail, but it became a " +
                "symbol of the search for another Earth.",
            sourceUrl = "https://en.wikipedia.org/wiki/Kepler-452b"
        ),
        SpaceFact(
            id = 25,
            title = "The James Webb Space Telescope",
            shortDescription = "An infrared eye reading the atmospheres of distant worlds.",
            detail = "The James Webb Space Telescope (JWST), launched in December 2021, is the most " +
                "powerful space observatory ever built. Operating in the infrared from a vantage point " +
                "1.5 million kilometers from Earth, it peers through cosmic dust and captures light from " +
                "the earliest galaxies. For exoplanets, its greatest gift is transmission spectroscopy: " +
                "as a planet crosses its star, JWST analyzes the starlight filtering through the " +
                "planet's atmosphere, detecting gases like water vapor, carbon dioxide, and methane. It " +
                "has already characterized exoplanet atmospheres in unprecedented detail, bringing the " +
                "search for habitable worlds and biosignatures within scientific reach.",
            sourceUrl = "https://en.wikipedia.org/wiki/James_Webb_Space_Telescope"
        )
    )

    fun byId(id: Int): SpaceFact? = all.firstOrNull { it.id == id }
}

package com.app.exoplanethunter.spacefacts

import android.content.Context
import org.json.JSONArray

/**
 * Loads the curated [SpaceFact] list from the bundled `space_facts.json` asset and caches it.
 *
 * Parsing happens lazily on first access and is then reused, so reading the asset only costs
 * once per process. Uses Android's built-in [org.json] so no JSON library dependency is needed.
 */
class SpaceFactProvider(private val context: Context) {

    private val facts: List<SpaceFact> by lazy { loadFromAsset() }

    fun all(): List<SpaceFact> = facts

    fun byId(id: Int): SpaceFact? = facts.firstOrNull { it.id == id }

    private fun loadFromAsset(): List<SpaceFact> = runCatching {
        val json = context.applicationContext.assets
            .open(ASSET_NAME)
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(json)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    SpaceFact(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        shortDescription = obj.getString("shortDescription"),
                        detail = obj.getString("detail"),
                        sourceUrl = obj.getString("sourceUrl")
                    )
                )
            }
        }
    }.getOrElse { emptyList() }

    companion object {
        private const val ASSET_NAME = "space_facts.json"
    }
}

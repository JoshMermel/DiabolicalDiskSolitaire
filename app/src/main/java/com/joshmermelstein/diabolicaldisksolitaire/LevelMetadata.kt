package com.joshmermelstein.diabolicaldisksolitaire

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class LevelMetadata(
    var next: String?,
    var displayId: String,
    var canonicalId: String,
)

// A singleton for holding metadata about levels
// |packData| for which levels are in packs
// |levelData| for par and which levels come after each other
class MetadataSingleton private constructor(context: Context) {
    private val levelData: MutableMap<String, LevelMetadata> = mutableMapOf()
    val packData: MutableMap<String, List<String>> = mutableMapOf()

    init {
        var prevId: String? = null
        for (filename in arrayOf(
           // "hotspot",
            "pent",
            "rect",
            "hex",
            "ring",
            "void",
            "fixed",
            "triangle",
           // "experiments"
        )) {
            val reader =
                BufferedReader(InputStreamReader(context.assets.open("packs/$filename.txt")))
            val title: String = reader.readLine()!!
            val pack: MutableList<String> = mutableListOf()

            var line: String? = reader.readLine()
            while (line != null) {
                val level = parseLevel(line)
                if (level != null) {
                    pack.add(level.canonicalId)
                    levelData[level.canonicalId] = level
                    levelData[prevId]?.next = level.canonicalId
                    prevId = level.canonicalId
                }
                line = reader.readLine()
            }
            packData[title] = pack
            prevId = null
        }
    }

    private fun parseLevel(line: String): LevelMetadata? {
        return LevelMetadata(null, line, line)
    }

    // Gets data about a level from its canonical ID.
    // For Infinity (randomly generated) levels, this creates a sensible response from thin air.
    fun getLevelData(id: String): LevelMetadata? {
        return levelData[id]
    }

    fun getNumComplete(packId: String): String {
        val levelIds = packData[packId] ?: return "0 / 0"

        return "0 / ${levelIds.size}"
    }

    companion object : SingletonHolder<MetadataSingleton, Context>(::MetadataSingleton)

}
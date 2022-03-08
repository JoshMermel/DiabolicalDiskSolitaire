package com.joshmermelstein.diabolicaldisksolitaire

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class LevelMetadata(
    var next: String?,
    var displayId: String,
    var canonicalId: String,
    var contents : GameplayParams,
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
            "linear",
            "ring12",
            "2x8",
            "3x4",
            "4x4",
            "hex",
            "triangle",
            "pent",
            // "void",
            // "fixed",
        )) {
            val reader =
                BufferedReader(InputStreamReader(context.assets.open("packs/$filename.txt")))
            val title: String = reader.readLine()!!
            val pack: MutableList<String> = mutableListOf()
            val type = reader.readLine()!!
            var dimension1 : Int? = null
            var dimension2 : Int? = null
            if (type == "RECT" || type == "HEX") {
                dimension1 = reader.readLine()!!.toInt()
                dimension2 = reader.readLine()!!.toInt()
            } else if (type == "RING") {
                dimension1 = reader.readLine()!!.toInt()
            }

            var lineNo = 0
            var line: String? = reader.readLine()
            while (line != null) {
                val name = "$filename-$lineNo"
                val level = LevelMetadata(null, name, name,
                    assembleParams(type, name, dimension1, dimension2, line.split(","))!!
                )
                pack.add(level.canonicalId)
                levelData[level.canonicalId] = level
                levelData[prevId]?.next = level.canonicalId
                prevId = level.canonicalId
                line = reader.readLine()
                lineNo++
            }
            packData[title] = pack
            prevId = null
        }
    }

    private fun assembleParams(type: String, id : String, dim1 : Int?, dim2 : Int?, board : List<String>) : GameplayParams? {
        return when (type) {
            "RECT" -> RectGameplayParams(id, dim1!!, dim2!!, board, 0)
            "HEX" -> HexGameplayParams(id, dim1!!, dim2!!, board, 0)
            "PENT" -> PentGameplayParams(id, board, 0)
            "RING" -> RingGameplayParams(id, dim1!!, board, 0)
            "TRIANGLE" -> TriangleGameplayParams(id, board, 0)
            else -> return null
        }
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
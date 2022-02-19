package com.joshmermelstein.diabolicaldisksolitaire

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

// Struct for what defines a level
sealed class GameplayParams(
    val id: String,
)

class RectGameplayParams(
    id: String,
    val numRows: Int,
    val numCols: Int,
    val board: List<String>,
    val winIdx : Int,
) : GameplayParams(id)

class HexGameplayParams(
    id: String,
    val numRows: Int,
    val numCols: Int,
    val board: List<String>,
    val winIdx : Int,
) : GameplayParams(id)

class PentGameplayParams(
    id: String,
    val board: List<String>,
    val winIdx : Int,
) : GameplayParams(id)

class RingGameplayParams(
    id: String,
    val size: Int,
    val board: List<String>,
    val winIdx : Int,
) : GameplayParams(id)

class TriangleGameplayParams(
    id: String,
    val board: List<String>,
    val winIdx : Int,
) : GameplayParams(id)

fun loadInitialLevel(id: String, context: Context): GameplayParams? {
    val reader: BufferedReader
    try {
        reader = BufferedReader(InputStreamReader(context.assets.open("levels/$id.txt")))
    } catch (ffe: java.io.FileNotFoundException) {
        return null
    }
    reader.use {
        return when (reader.readLine()) {
            "RECT" -> loadInitialLevelRect(it, id)
            "HEX" -> loadInitialLevelHex(it, id)
            "PENT" -> loadInitialLevelPent(it, id)
            "RING" -> loadInitialLevelRing(it, id)
            "TRIANGLE" -> loadInitialLevelTriangle(it, id)
            else -> return null
        }
    }
}

// TODO(jmerm): methods below should return null instead of crashing on bad input.
fun loadInitialLevelRect(reader: BufferedReader, id: String): RectGameplayParams? {
    val numRows = reader.readLine().toInt()
    val numCols = reader.readLine().toInt()
    val board = reader.readLine().split(",")
    val winIdx = reader.readLine().toInt()
    return RectGameplayParams(
        id, numRows, numCols, board, winIdx
    )
}

fun loadInitialLevelHex(reader: BufferedReader, id: String): HexGameplayParams? {
    val numRows = reader.readLine().toInt()
    val numCols = reader.readLine().toInt()
    val board = reader.readLine().split(",")
    val winIdx = reader.readLine().toInt()
    return HexGameplayParams(
        id, numRows, numCols, board, winIdx
    )
}

fun loadInitialLevelPent(reader: BufferedReader, id: String): PentGameplayParams? {
    val board = reader.readLine().split(",")
    val winIdx = reader.readLine().toInt()
    return PentGameplayParams(id, board, winIdx)
}

fun loadInitialLevelRing(reader: BufferedReader, id: String): RingGameplayParams? {
    val size = reader.readLine().toInt()
    val board = reader.readLine().split(",")
    val winIdx = reader.readLine().toInt()
    return RingGameplayParams(id, size, board, winIdx)
}
fun loadInitialLevelTriangle(reader: BufferedReader, id: String): TriangleGameplayParams? {
    val board = reader.readLine().split(",")
    val winIdx = reader.readLine().toInt()
    return TriangleGameplayParams(id, board, winIdx)
}

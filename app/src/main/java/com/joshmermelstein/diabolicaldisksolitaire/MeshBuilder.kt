package com.joshmermelstein.diabolicaldisksolitaire

import android.content.Context
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Helpers for building meshes of various kinds.

// 0 1
// 2 3
// 4 5
fun makeRectBoard(
    context: Context,
    params : RectGameplayParams
): Board {
    val lanes = buildList {
        for (row in (0 until params.numRows)) {
            add((row * params.numCols until (row + 1) * params.numCols).toList())
        }
        for (col in (0 until params.numCols)) {
            add((col until (params.numRows * params.numCols) step params.numCols).toList())
        }
    }

    val boardState = params.board.map { makeCheapDisk(it) }.toMutableList()
    val boardLogic = BoardLogic(lanes)

    val cellBounds = buildList {
        for (row in 0 until params.numRows) {
            for (col in 0 until params.numCols) {
                add(
                    listOf(
                        Pt(100f * col, 100f * row),
                        Pt(100f * (col + 1), 100f * row),
                        Pt(100f * (col + 1), 100f * (row + 1)),
                        Pt(100f * col, 100f * (row + 1))
                    ),
                )
            }
        }
    }


    val sideLen = 100f
    val metadata = mapOf(1 to sideLen * 0.32f, 2 to sideLen * 0.48f, 3 to sideLen * 0.64f)

    val layoutParams = CheapBoardLayoutParams(cellBounds, metadata, DiskColors(context))
    return Board(boardState, boardLogic, layoutParams, params.winIdx)
}

// TODO(jmerm): can this row/lane computation get factored out into some nice helpers?
fun makeHexBoard(
    context: Context,
    params: HexGameplayParams
): Board {
    val lanes = buildList {
        // diagonal down/right
        for (row in (0 until params.numRows)) {
            add((row * params.numCols until (row + 1) * params.numCols).toList())
        }
        // down
        for (col in (0 until params.numCols)) {
            add((col until (params.numRows * params.numCols) step params.numCols).toList())
        }
        // diagonal up/right from left bound
        for (id in (0 until (params.numRows * params.numCols) step params.numCols)) {
            var col = id % params.numCols
            var row = (id - col) / params.numCols
            while (col in (0 until params.numCols) && row in (0 until params.numRows)) {
                row -= 1
                col += 1
            }
            val dstIdx = (row * params.numCols) + col
            add((id downTo dstIdx + 1 step (params.numCols - 1)).toList())
        }
        // diagonal up/right from bottom-left bound
        for (id in ((params.numRows - 1) * params.numCols until (params.numRows * params.numCols))) {
            var col = id % params.numCols
            var row = (id - col) / params.numCols
            while (col in (0 until params.numCols) && row in (0 until params.numRows)) {
                row -= 1
                col += 1
            }
            val dstIdx = (row * params.numCols) + col
            add((id downTo dstIdx + 1 step (params.numCols - 1)).toList())
        }
    }.distinct()

    val boardState = params.board.map { makeCheapDisk(it) }.toMutableList()
    val boardLogic = BoardLogic(lanes)

    val hexWidth = 100f
    val hexHeight = 50f * sqrt(3f)

    val cellBounds = buildList {
        for (row in 0 until params.numRows) {
            val rowVOffset = hexHeight * 2 * row
            for (col in 0 until params.numCols) {
                val colVOffset = hexHeight * col
                val colHOffset = 1.5f * hexWidth * col
                add(
                    listOf(
                        Pt(colHOffset - hexWidth / 2, colVOffset + rowVOffset - hexHeight),
                        Pt(colHOffset + hexWidth / 2, colVOffset + rowVOffset - hexHeight),
                        Pt(colHOffset + hexWidth, colVOffset + rowVOffset),
                        Pt(colHOffset + hexWidth / 2, colVOffset + rowVOffset + hexHeight),
                        Pt(colHOffset - hexWidth / 2, colVOffset + rowVOffset + hexHeight),
                        Pt(colHOffset - hexWidth, colVOffset + rowVOffset),
                    ),
                )
            }
        }
    }

    val metadata = mapOf(1 to 35f, 2 to 75f, 3 to 115f)
    val layoutParams = CheapBoardLayoutParams(cellBounds, metadata, DiskColors(context))
    return Board(boardState, boardLogic, layoutParams, params.winIdx)
}

fun makePentBoard(context: Context, params : PentGameplayParams): Board {
    val lanes = listOf(
        listOf(19, 9, 3, 0),
        listOf(18, 8, 2, 1),
        listOf(17, 7, 6, 12),
        listOf(16, 15, 14, 13),
        listOf(0, 1, 4, 10),
        listOf(3, 2, 5, 11),
        listOf(19, 18, 17, 16),
        listOf(9, 8, 7, 15),
        listOf(4, 5, 6, 14),
        listOf(10, 11, 12, 13),
    )

    val points = arrayOf(
        Pt(152.193f, 0f),
        Pt(114.126f, 27.39f),
        Pt(151.648f, 69.203f),
        Pt(190.211f, 27.646f),
        Pt(76.085f, 55.284f),
        Pt(110.968f, 103.294f),
        Pt(152.168f, 160.003f),
        Pt(193.374f, 103.296f),
        Pt(228.256f, 55.29f),
        Pt(38.043f, 82.925f),
        Pt(67.039f, 131.678f),
        Pt(87.26f, 181.09f),
        Pt(99.804f, 232.11f),
        Pt(152.166f, 228.255f),
        Pt(204.541f, 232.089f),
        Pt(217.08f, 181.099f),
        Pt(237.623f, 131.04f),
        Pt(266.298f, 82.93f),
        Pt(0f, 110.559f),
        Pt(14.531f, 155.28f),
        Pt(29.061f, 200.001f),
        Pt(43.593f, 244.73f),
        Pt(58.123f, 289.451f),
        Pt(105.145f, 289.451f),
        Pt(152.168f, 289.451f),
        Pt(199.19f, 289.451f),
        Pt(246.213f, 289.541f),
        Pt(260.744f, 240.72f),
        Pt(275.289f, 200.01f),
        Pt(289.809f, 155.29f),
        Pt(304.34f, 110.569f),
    )

    val cellBounds = listOf(
        listOf(0, 3, 2, 1),
        listOf(3, 8, 7, 2),
        listOf(2, 7, 6, 5),
        listOf(1, 2, 5, 4),
        listOf(8, 17, 16, 7),
        listOf(6, 7, 16, 15),
        listOf(6, 15, 14, 13),
        listOf(11, 6, 13, 12),
        listOf(10, 5, 6, 11),
        listOf(4, 5, 10, 9),
        listOf(17, 30, 29, 16),
        listOf(16, 29, 28, 15),
        listOf(15, 28, 27, 14),
        listOf(14, 27, 26, 25),
        listOf(13, 14, 25, 24),
        listOf(12, 13, 24, 23),
        listOf(21, 12, 23, 22),
        listOf(20, 11, 12, 21),
        listOf(19, 10, 11, 20),
        listOf(18, 9, 10, 19),
    ).map { outline -> outline.map { points[it] } }

    val boardState = params.board.map { makeCheapDisk(it) }.toMutableList()
    val boardLogic = BoardLogic(lanes)
    val metadata = mapOf(1 to 10f, 3 to 36f)

    val layoutParams = CheapBoardLayoutParams(cellBounds, metadata, DiskColors(context))
    return Board(boardState, boardLogic, layoutParams, params.winIdx)
}

fun fromPolar(r: Double, theta: Double): Pt =
    Pt((r * sin(theta)).toFloat(), (r * cos(theta)).toFloat())

fun <T> Array<T>.rightCycle(d: Int): Array<T> {
    val n = d % size
    if (n == 0) return this
    return sliceArray(size - n until size) + sliceArray(0 until size - n)
}

fun makeRingBoard(context: Context, params: RingGameplayParams): Board {
    val lanes = List(params.size) { ((0 until params.size).toList().toTypedArray().rightCycle(it)).toList() }

    val innerArc = 200 * Math.PI / params.size
    val arcStep = 2 * Math.PI / params.size
    val cellBounds = List(params.size) {
        listOf(
            fromPolar(100.0 - (innerArc / 2), Math.PI + (arcStep * (it + 0))),
            fromPolar(100.0 + (innerArc / 2), Math.PI + (arcStep * (it + 0))),
            fromPolar(100.0 + (innerArc / 2), Math.PI + (arcStep * (it + 1))),
            fromPolar(100.0 - (innerArc / 2), Math.PI + (arcStep * (it + 1))),
        )
    }


    val boardState = params.board.map { makeCheapDisk(it) }.toMutableList()
    val boardLogic = BoardLogic(lanes)
    val metadata = mapOf(1 to 16f, 2 to 24f, 3 to 32f)

    val layoutParams = CheapBoardLayoutParams(cellBounds, metadata, DiskColors(context))
    return Board(boardState, boardLogic, layoutParams, params.winIdx)
}

fun makeTriangleBoard(context: Context, params : TriangleGameplayParams): Board {
    val lanes = listOf(
        listOf(0, 1, 4, 8),
        listOf(3, 2, 5, 9),
        listOf(0, 3, 7, 11),
        listOf(1, 2, 6, 10),
        listOf(4, 5, 6, 7),
        listOf(8, 9, 10, 11),
    )

    val points = arrayOf(
        Pt(166.301f, 0f),
        Pt(229.598f, 46.786f),
        Pt(166.299f, 85.167f),
        Pt(103.448f, 46.977f),
        Pt(288.021f, 121.728f),
        Pt(227.161f, 156.863f),
        Pt(166.3f, 191.998f),
        Pt(105.438f, 156.863f),
        Pt(44.576f, 121.726f),
        Pt(323.218f, 210.155f),
        Pt(258.819f, 245.416f),
        Pt(166.3f, 262.276f),
        Pt(73.779f, 245.414f),
        Pt(9.442f, 210.198f),
        Pt(332.577f, 288f),
        Pt(260.467f, 318.943f),
        Pt(166.3f, 332.554f),
        Pt(72.125f, 318.892f),
        Pt(0.023f, 288f),
    )

    val cellBounds = listOf(
        listOf(0, 3, 2, 1),
        listOf(1, 2, 5, 4),
        listOf(2, 7, 6, 5),
        listOf(3, 8, 7, 2),
        listOf(5, 10, 9, 4),
        listOf(6, 11, 10, 5),
        listOf(6, 7, 12, 11),
        listOf(8, 13, 12, 7),
        listOf(10, 15, 14, 9),
        listOf(11, 16, 15, 10),
        listOf(12, 17, 16, 11),
        listOf(13, 18, 17, 12),
    ).map { outline -> outline.map { points[it] } }

    val boardState = params.board.map { makeCheapDisk(it) }.toMutableList()
    val boardLogic = BoardLogic(lanes)
    val metadata = mapOf(1 to 10f, 2 to 30f, 3 to 50f)

    val layoutParams = CheapBoardLayoutParams(cellBounds, metadata, DiskColors(context))
    return Board(boardState, boardLogic, layoutParams, params.winIdx)
}

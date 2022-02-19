package com.joshmermelstein.diabolicaldisksolitaire

import android.graphics.Canvas

// TODO(jmerm): figure out why the scaler needs to be lateinit. Can that be fixed?

class Board(
    private val entries : MutableList<CheapDisk>,
    private val boardLogic: BoardLogic,
    private val boardLayout: BoardLayout,
    private val winIdx : Int
) {
    private lateinit var scaler: MeshScaler

    // Updates how we translate between virtual coordinates and screen coordinates.
    fun updateBounds(bounds: Bounds) {
        scaler = MeshScaler(
            boardLayout.virtualLeft,
            boardLayout.virtualTop,
            boardLayout.virtualBottom,
            bounds
        )
    }

    // Draws the board to |canvas| such that it fits in |bounds| as well as possible.
    fun drawSelf(canvas: Canvas, bounds: Bounds) {
        if (!::scaler.isInitialized) {
            scaler = MeshScaler(
                boardLayout.virtualLeft,
                boardLayout.virtualTop,
                boardLayout.virtualBottom,
                bounds
            )
        }
        boardLayout.drawCells(scaler, canvas, entries, winIdx)
        boardLayout.drawDisks(scaler, canvas, entries)
    }

    // (height/width) of what sort of rectangle should hold the board.
    val virtualWidth = boardLayout.virtualWidth
    val virtualHeight = boardLayout.virtualHeight

    // Callback for when the user touches the screen.
    fun handleDownInput(absoluteX: Float, absoluteY: Float) {
        val touchedCell = boardLayout.getTouchedCell(scaler, absoluteX, absoluteY) ?: return
        val disk = entries[touchedCell]
        if (disk.size == 0 || disk.isFixed || disk.isVoid) {
            return
        }
        boardLayout.heldDiskIdx = touchedCell
        boardLayout.heldDiskPos = Pt(absoluteX, absoluteY)
    }

    // Callback for when the user moves their finger after touching the screen.
    fun handleMoveInput(absoluteX: Float, absoluteY: Float) {
        if (boardLayout.heldDiskIdx != null) {
            boardLayout.heldDiskPos = Pt(absoluteX, absoluteY)
        }
    }

    // Callback for when the user stops touching the screen.
    fun handleUpInput(absoluteX: Float, absoluteY: Float): Move? {
        val srcIdx: Int = boardLayout.heldDiskIdx ?: return null
        val dstIdx = boardLayout.getTouchedCell(scaler, absoluteX, absoluteY)

        return if (dstIdx == null || !boardLogic.getValidDestinations(entries, srcIdx)
                .contains(dstIdx)
        ) {
            boardLayout.snapBack()
            null
        } else {
            entries.swap(srcIdx, dstIdx)
            boardLayout.snapBack()
            Move(srcIdx, dstIdx)
        }
    }

    // Uses BFS to solve the board and returns the optimal next move if there is one.
    fun help() : Move? {
        val solution = solve(entries, boardLogic, winIdx)
        if (solution.isNotEmpty()) {
            entries.swap(solution[0].src, solution[0].dst)
            return solution[0]
        }
        return null
    }

    // Applies a move to the board.
    fun applyMove(move: Move) = entries.swap(move.src, move.dst)

    // Returns whether the board is currently solved, assuming |winIdx| is the goal spot.
    fun isSolved(): Boolean = boardLogic.isSolved(entries, winIdx)
}


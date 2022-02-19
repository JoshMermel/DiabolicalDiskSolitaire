package com.joshmermelstein.diabolicaldisksolitaire

import android.graphics.Canvas

// TODO(jmerm): figure out why the scaler needs to be lateinit. Can that be fixed?

class Board(
    private val entries : MutableList<CheapDisk>,
    private val boardLogic: BoardLogic,
    private val layoutParams: CheapBoardLayoutParams,
    private val winIdx : Int
) {

    private lateinit var scaler: MeshScaler
    fun updateBounds(bounds: Bounds) {
        scaler = MeshScaler(
            layoutParams.virtualLeft,
            layoutParams.virtualTop,
            layoutParams.virtualBottom,
            bounds
        )
    }

    fun drawSelf(canvas: Canvas, bounds: Bounds) {
        if (!::scaler.isInitialized) {
            scaler = MeshScaler(
                layoutParams.virtualLeft,
                layoutParams.virtualTop,
                layoutParams.virtualBottom,
                bounds
            )
        }
        layoutParams.drawCells(scaler, canvas, entries, winIdx)
        layoutParams.drawDisks(scaler, canvas, entries)
    }

    // (height/width) of what sort of rectangle should hold the board.
    val virtualWidth = layoutParams.virtualWidth
    val virtualHeight = layoutParams.virtualHeight

    fun handleDownInput(absoluteX: Float, absoluteY: Float) {
        val touchedCell = layoutParams.getTouchedCell(scaler, absoluteX, absoluteY) ?: return
        val disk = entries[touchedCell]
        if (disk.size == 0 || disk.isFixed || disk.isVoid) {
            return
        }
        layoutParams.heldDiskIdx = touchedCell
        layoutParams.heldDiskPos = Pt(absoluteX, absoluteY)
    }

    fun handleMoveInput(absoluteX: Float, absoluteY: Float) {
        if (layoutParams.heldDiskIdx != null) {
            layoutParams.heldDiskPos = Pt(absoluteX, absoluteY)
        }
    }

    fun handleUpInput(absoluteX: Float, absoluteY: Float): Move? {
        val srcIdx: Int = layoutParams.heldDiskIdx ?: return null
        val dstIdx = layoutParams.getTouchedCell(scaler, absoluteX, absoluteY)

        return if (dstIdx == null || !boardLogic.getValidDestinations(entries, srcIdx)
                .contains(dstIdx)
        ) {
            layoutParams.snapBack()
            null
        } else {
            entries.swap(srcIdx, dstIdx)
            layoutParams.snapBack()
            Move(srcIdx, dstIdx)
        }
    }

    fun help() : Move? {
        val solution = solve(entries, boardLogic, winIdx)
        if (solution.isNotEmpty()) {
            entries.swap(solution[0].src, solution[0].dst)
            return solution[0]
        }
        return null
    }

    fun applyMove(move: Move) = entries.swap(move.src, move.dst)
    fun isSolved(): Boolean = boardLogic.isSolved(entries, winIdx)
}


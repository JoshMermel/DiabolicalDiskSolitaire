package com.joshmermelstein.diabolicaldisksolitaire

import android.graphics.Canvas

abstract class Board {
    abstract fun drawSelf(canvas: Canvas, bounds: Bounds)
    abstract fun updateBounds(bounds: Bounds)

    // (height/width) of what sort of rectangle should hold the board.
    abstract val virtualWidth: Float
    abstract val virtualHeight: Float

    abstract fun handleDownInput(absoluteX: Float, absoluteY: Float)
    abstract fun handleMoveInput(absoluteX: Float, absoluteY: Float)
    abstract fun handleUpInput(absoluteX: Float, absoluteY: Float): Move?

    abstract fun applyMove(move : Move)

    abstract fun isSolved() : Boolean
}
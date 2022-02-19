package com.joshmermelstein.diabolicaldisksolitaire

class Bounds(
    val left: Float,
    val top: Float,
    private val right: Float,
    private val bottom: Float
) {
    fun width(): Float {
        return right - left
    }

    fun height(): Float {
        return bottom - top
    }
}

data class Move(val src: Int, val dst: Int)

fun MutableList<CheapDisk>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

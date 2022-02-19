package com.joshmermelstein.diabolicaldisksolitaire

class MeshScaler(
    private val left: Float,
    private val top: Float,
    bottom: Float,
    private val bounds: Bounds
) {
    private val scale = bounds.height() / (bottom - top)

    // Converts from virtual coordinates to screen coordinates
    fun scaleX(x: Float): Int = (bounds.left + (scale * (x - left))).toInt()
    fun scaleY(y: Float): Int = (bounds.top + (scale * (y - top))).toInt()
    fun scaleVal(v: Float): Int = (scale * v).toInt()

    fun unScaleX(x: Float): Float = ((x - bounds.left) / scale) + left
    fun unScaleY(y: Float): Float = ((y - bounds.top) / scale) + top
}
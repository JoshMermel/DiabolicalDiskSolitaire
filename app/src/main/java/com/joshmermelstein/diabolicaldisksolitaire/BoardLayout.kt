package com.joshmermelstein.diabolicaldisksolitaire

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.PathShape

data class Pt(val x: Float, val y: Float)

// Class that understands how to draw a board to a screen.
// This lets us decouple gameplay logic from UI concerns.
class BoardLayout(
    cellBounds: List<List<Pt>>,
    private val diskMetadata: DiskMetadata,
    private val diskColors: DiskColors
) {
    var heldDiskPos: Pt? = null
    var heldDiskIdx: Int? = null
    private val cells = cellBounds.map { JCell(it) }

    // TODO(jmerm): these don't account for voids.
    val virtualLeft = cellBounds.minOf { cell -> cell.minOf { it.x } }
    private val virtualRight = cellBounds.maxOf { cell -> cell.maxOf { it.x } }
    val virtualTop = cellBounds.minOf { cell -> cell.minOf { it.y } }
    val virtualBottom = cellBounds.maxOf { cell -> cell.maxOf { it.y } }
    val virtualWidth = virtualRight - virtualLeft
    val virtualHeight = virtualBottom - virtualTop

    // Returns the offset of the touched cell if there was one.
    fun getTouchedCell(scaler: MeshScaler, eventX: Float, eventY: Float): Int? {
        for (i in cells.indices) {
            if (cells[i].containsPoint(scaler.unScaleX(eventX), scaler.unScaleY(eventY))) {
                return i
            }
        }
        return null
    }

    fun drawCells(scaler: MeshScaler, canvas: Canvas, entries: List<CheapDisk>, winIdx: Int) {
        for (i in cells.indices) {
            val disk = entries[i]
            if (!disk.isVoid) {
                cells[i].drawSelf(scaler, canvas, i == winIdx)
            }
        }
    }

    fun drawDisks(scaler: MeshScaler, canvas: Canvas, entries : List<CheapDisk>) {
        for (i in cells.indices) {
            val disk = entries[i]
            if (disk.isVoid) {
                continue
            }
            val radius = scaler.scaleVal(diskMetadata[disk.size] ?: continue)
            val color = when {
                disk.isWin -> diskColors.gold
                disk.isFixed -> diskColors.fixed
                disk.size == 3 -> diskColors.green
                disk.size == 2 -> diskColors.blue
                disk.size == 1 -> diskColors.red
                else -> Color.BLACK
            }
            if (i == heldDiskIdx && heldDiskPos != null) {
                drawCircle(
                    canvas,
                    heldDiskPos!!.x.toInt(),
                    heldDiskPos!!.y.toInt(),
                    radius,
                    color
                )
            } else {
                drawCircle(
                    canvas,
                    scaler.scaleX(cells[i].virtualCenterX),
                    scaler.scaleY(cells[i].virtualCenterY),
                    radius,
                    color
                )
            }
        }
    }

    // TODO(jmerm): I think we don't want to be recreating this oval shape every time.
    //              Also most of this should be cacheable.
    private fun drawCircle(
        canvas: Canvas,
        centerX: Int,
        centerY: Int,
        radius: Int,
        color: Int
    ) {
        val disk = ShapeDrawable(OvalShape())
        disk.setBounds(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        disk.paint.color = color
        disk.alpha = 192
        disk.draw(canvas)
    }

    fun snapBack() {
        heldDiskIdx = null
        heldDiskPos = null
    }
}

// A JCell represents a single cell drawn on screen.
class JCell(private val points: List<Pt>) {
    // Relative size info about the cell
    private val virtualLeft = points.minOf { it.x }
    private val virtualRight = points.maxOf { it.x }
    private val virtualTop = points.minOf { it.y }
    private val virtualBottom = points.maxOf { it.y }
    private val virtualWidth = virtualRight - virtualLeft
    private val virtualHeight = virtualBottom - virtualTop
    val virtualCenterX = points.map { it.x }.average().toFloat()
    val virtualCenterY = points.map { it.y }.average().toFloat()

    // A drawable shape matching the path defined by |points| and flush with y=0 and x=0.
    // This is by lazy to make testing more convenient. It means that tests which
    // don't use this shape don't need to do any graphics mocking.
    private val outline: ShapeDrawable by lazy { makeOutlineShape() }

    private fun makePath(): Path {
        return Path().also {
            it.moveTo(points[0].x - virtualLeft, points[0].y - virtualTop)
            for (point in points) {
                it.lineTo(point.x - virtualLeft, point.y - virtualTop)
            }
            it.close()
        }
    }

    private fun makeOutlineShape(): ShapeDrawable {
        return ShapeDrawable(PathShape(makePath(), virtualWidth, virtualHeight)).apply {
            paint.color = Color.LTGRAY
        }
    }

    // https://web.archive.org/web/20161108113341/https://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
    fun containsPoint(x: Float, y: Float): Boolean {
        var i = 0
        var j = points.size - 1
        var result = false

        while (i < points.size) {
            if ((points[i].y > y) != (points[j].y > y) &&
                (x < (points[j].x - points[i].x) * (y - points[i].y) / (points[j].y - points[i].y) + points[i].x)
            ) {
                result = !result
            }
            j = i++
        }
        return result
    }

    fun drawSelf(scaler: MeshScaler, canvas: Canvas, isWin: Boolean) {
        val padding = 3
        outline.setBounds(
            scaler.scaleX(virtualLeft) + padding,
            scaler.scaleY(virtualTop) + padding,
            scaler.scaleX(virtualRight) - padding,
            scaler.scaleY(virtualBottom) - padding
        )
        if (isWin) {
            outline.paint.color = Color.rgb(255, 231, 163)
        }
        outline.draw(canvas)
    }

    override fun toString(): String = "{${points.joinToString(",")}}"
}
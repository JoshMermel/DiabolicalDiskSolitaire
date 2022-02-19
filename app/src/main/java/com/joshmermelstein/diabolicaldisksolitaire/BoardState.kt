package com.joshmermelstein.diabolicaldisksolitaire

// TODO(jmerm): figure out why the meshscaler needs to be lateinit. Can that be fixed?
// TODO(jmerm): break this up into many smaller files

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.PathShape

data class Pt(val x: Float, val y: Float)
data class CheapDisk(
    val size: Int,
    val isWin: Boolean = false,
    val isFixed: Boolean = false,
    val isVoid: Boolean = false
)

fun MutableList<CheapDisk>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

class JBoard(
    private val entries : MutableList<CheapDisk>,
    private val boardLogic: BoardLogic,
    private val layoutParams: CheapBoardLayoutParams,
    private val winIdx : Int
) :
    Board() {

    private lateinit var scaler: MeshScaler
    override fun updateBounds(bounds: Bounds) {
        scaler = MeshScaler(
            layoutParams.virtualLeft,
            layoutParams.virtualTop,
            layoutParams.virtualBottom,
            bounds
        )
    }

    override fun drawSelf(canvas: Canvas, bounds: Bounds) {
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
    override val virtualWidth = layoutParams.virtualWidth
    override val virtualHeight = layoutParams.virtualHeight

    override fun handleDownInput(absoluteX: Float, absoluteY: Float) {
        val touchedCell = layoutParams.getTouchedCell(scaler, absoluteX, absoluteY) ?: return
        val disk = entries[touchedCell]
        if (disk.size == 0 || disk.isFixed || disk.isVoid) {
            return
        }
        layoutParams.heldDiskIdx = touchedCell
        layoutParams.heldDiskPos = Pt(absoluteX, absoluteY)
    }

    override fun handleMoveInput(absoluteX: Float, absoluteY: Float) {
        if (layoutParams.heldDiskIdx != null) {
            layoutParams.heldDiskPos = Pt(absoluteX, absoluteY)
        }
    }

    override fun handleUpInput(absoluteX: Float, absoluteY: Float): Move? {
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

    override fun help() {
        val solution = solve(entries, boardLogic, winIdx)
        if (solution != null) {
            entries.swap(solution.src, solution.dst)
        }
    }

    override fun applyMove(move: Move) = entries.swap(move.src, move.dst)
    override fun isSolved(): Boolean = boardLogic.isSolved(entries, winIdx)
}

fun makeCheapDisk(spec: String): CheapDisk {
    val parts = spec.split(" ")

    var isWin = false
    var isFixed = false
    var isVoid = false
    var size = 0

    for (part in parts) {
        when (part) {
            "F" -> isFixed = true
            "G" -> isWin = true
            "V" -> isVoid = true
            "0" -> size = 0
            "1" -> size = 1
            "2" -> size = 2
            "3" -> size = 3
        }
    }

    return CheapDisk(size, isWin, isFixed, isVoid)
}

// TODO(jmerm): consider adding a kind of "circular lane" logic handling
// TODO(jmerm): rename "rows" row "lanes".
class BoardLogic(rows: List<List<Int>>) {
    private val numEntries: Int = 1 + rows.maxOf { it.maxOrNull() ?: 0 }
    private val rays = computeRays(rows)
    private fun computeRays(rows: List<List<Int>>): List<List<List<Int>>> {
        val ret = MutableList<MutableList<List<Int>>>(numEntries) { mutableListOf() }
        for (row in rows) {
            for (i in row.indices) {
                val tmp = row.slice((i + 1 until row.size))
                if (tmp.isNotEmpty()) {
                    ret[row[i]].add(tmp)
                }
                val reverseTmp = row.asReversed().slice(i + 1 until row.size)
                if (reverseTmp.isNotEmpty()) {
                    ret[row[row.size - i - 1]].add(reverseTmp)
                }
            }
        }
        return ret
    }

    private fun getDestinationAlongRay(entries: List<CheapDisk>, idx: Int, ray: List<Int>): Int? {
        if (entries[idx].size == 0 || entries[idx].isVoid || entries[idx].isFixed) {
            // nothing to move
            return null
        }
        if (ray.isEmpty() || entries[ray[0]].size == 0 || entries[ray[0]].isVoid) {
            // nothing to jump over!
            return null
        }
        for (dst in ray.drop(1)) {
            if (entries[dst].size == 0) {
                return if (isValidDest(entries, dst, entries[idx]) && !entries[dst].isVoid) {
                    dst
                } else {
                    null
                }
            }
        }
        return null
    }

    private fun isValidDest(entries: List<CheapDisk>, idx: Int, disk: CheapDisk): Boolean {
        val maxSize = 4 - disk.size
        return rays[idx].map { entries[it[0]].size <= maxSize }.all { it }
    }

    fun getValidDestinations(entries: List<CheapDisk>, idx: Int): List<Int> =
        rays[idx].mapNotNull { getDestinationAlongRay(entries, idx, it) }

    fun isSolved(entries: List<CheapDisk>, winIdx: Int): Boolean = entries[winIdx].isWin
}

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

// Note, pairs are in (x,y) format.
class CheapBoardLayoutParams(
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
        // TODO(jmerm): this transparency thing is a hack. Just draw the held one last.
        disk.alpha = 192
        disk.draw(canvas)
    }

    fun snapBack() {
        heldDiskIdx = null
        heldDiskPos = null
    }
}
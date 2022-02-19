package com.joshmermelstein.diabolicaldisksolitaire

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

//    private fun drawIcon(
//        icon: Drawable,
//        left: Int,
//        top: Int,
//        right: Int,
//        bottom: Int,
//        canvas: Canvas
//    ) {
//        icon.setBounds(left, top, right, bottom)
//        // TODO(jmerm): white color is wrong in dark mode
//        DrawableCompat.setTint(icon.mutate(), Color.WHITE)
//        icon.draw(canvas)
//    }

// TODO(jmerm): rename
typealias DiskMetadata = Map<Int, Float>

open class DiskColors(
    val red: Int,
    val blue: Int,
    val green: Int,
    val gold: Int,
    val fixed: Int,
    val lock: Drawable
) {
    constructor(context: Context) : this(
        ContextCompat.getColor(context, R.color.red_disk),
        ContextCompat.getColor(context, R.color.blue_disk),
        ContextCompat.getColor(context, R.color.green_disk),
        ContextCompat.getColor(context, R.color.gold_disk),
        ContextCompat.getColor(context, R.color.fixed_disk),
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_baseline_lock_24, null)!!
    )
}

class FakeDiskColors :
    DiskColors(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, ShapeDrawable())
package com.joshmermelstein.diabolicaldisksolitaire

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

// A custom View class for managing interactions between the canvas and gameplay logic.
class GameplayView : View {
    lateinit var gameManager: GameManager

    // Cache of boundaries for drawing the board.
    private lateinit var boundsBoard: Bounds

    // TODO(jmerm): Check that this handles resize nicely.
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        placeBoard(left, top, right, bottom)
        gameManager.updateBounds(boundsBoard)
        super.onLayout(changed, left, top, right, bottom)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun placeBoard(viewLeft: Int, viewTop: Int, viewRight: Int, viewBottom: Int) {
        val viewWidth = viewRight - viewLeft
        val viewHeight = viewBottom - viewTop

        // Do a candidate layout at full width
        var gameplayTop = 0f
        var gameplayLeft = 0f
        var gameplayRight = viewRight.toFloat()
        var gameplayBottom =
            viewTop + (viewWidth * gameManager.board.virtualHeight / gameManager.board.virtualWidth)
        boundsBoard = Bounds(10f, 10f, viewWidth / 2f, viewHeight / 2f)

        // If that layout caused overflow, scale down until we fit
        if (gameplayBottom > viewBottom) {
            gameplayRight = (viewRight * viewHeight / (gameplayBottom - viewTop))
            gameplayBottom = viewBottom.toFloat()
            // also center horizontally
            val margin = viewWidth - gameplayRight
            gameplayLeft += margin / 2
            gameplayRight += margin / 2
        } else {
            // also center vertically
            val margin = viewBottom - gameplayBottom
            gameplayTop += margin / 2
            gameplayBottom += margin / 2
        }

        boundsBoard = Bounds(gameplayLeft, gameplayTop, gameplayRight, gameplayBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        gameManager.update()
        gameManager.drawBoard(canvas, boundsBoard)

        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> gameManager.handleDownInput(event.x, event.y)
            MotionEvent.ACTION_MOVE -> gameManager.handleMoveInput(event.x, event.y)
            MotionEvent.ACTION_UP -> gameManager.handleUpInput(event.x, event.y)
            else -> super.onTouchEvent(event)
        }
    }
}
package com.joshmermelstein.diabolicaldisksolitaire

import android.app.Dialog
import android.graphics.Canvas
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.*

// Manages the logic of the the game and produces frames to be rendered on demand.
class GameManager(
    private val params: GameplayParams,
    private val context: AppCompatActivity,
    private val buttonState: ButtonState
) {
    // TODO(jmerm): should factory methods just take GameplayParams?
    var board = when (params) {
        is RectGameplayParams -> makeRectJBoard(context, params)
        is HexGameplayParams -> makeHexJBoard(context, params)
        is PentGameplayParams -> makePentJBoard(context, params)
        is RingGameplayParams -> makeRingJBoard(context, params)
        is TriangleGameplayParams -> makeTriangleJBoard(context, params)
    }

    private var undoStack = Stack<Move>()
    private var redoStack = Stack<Move>()

    private var numMoves: Int = 0
    private var complete = false

    init {
        buttonState.undoButtonEnabled = false
        buttonState.redoButtonEnabled = false
    }


    // Updates the draw positions of game elements and checks for wins. Uses system time to
    // determine positions so that animation speed doesn't depend on frames per second.
    fun update() {
        if (!complete && isSolved()) {
            complete = true
            winDialog()
        }
    }

    fun updateBounds(bounds: Bounds) {
        board.updateBounds(bounds)
    }

    fun drawBoard(canvas: Canvas, boundsBoard: Bounds) = board.drawSelf(canvas, boundsBoard)

    private fun isSolved(): Boolean = board.isSolved()

    fun handleDownInput(absoluteX: Float, absoluteY: Float): Boolean {
        board.handleDownInput(absoluteX, absoluteY)
        return true
    }

    fun handleMoveInput(absoluteX: Float, absoluteY: Float): Boolean {
        board.handleMoveInput(absoluteX, absoluteY)
        return true
    }

    fun handleUpInput(absoluteX: Float, absoluteY: Float): Boolean {
        board.handleUpInput(absoluteX, absoluteY)?.also {
            undoStack.add(it)
            numMoves++
            updateButtons()
        }
        return true
    }

    private fun updateButtons() {
        buttonState.undoButtonEnabled = undoStack.isNotEmpty()
        buttonState.redoButtonEnabled = redoStack.isNotEmpty()
        context.invalidateOptionsMenu()
    }

    fun undoMove() {
        if (undoStack.empty()) {
            return
        }
        board.applyMove(undoStack.peek())
        redoStack.push(undoStack.pop())
        numMoves--
        updateButtons()
    }

    fun redoMove() {
        if (redoStack.empty()) {
            return
        }
        board.applyMove(redoStack.peek())
        undoStack.push(redoStack.pop())
        numMoves++
        updateButtons()
    }


    // Used for generating save files.
    override fun toString(): String {
        // TODO(jmerm): implement toString on game manager
        return "TODO(jmerm)"
    }

    // Displays a dialog when the user wins.
    private fun winDialog() {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.win_popup)

        dialog.findViewById<Button>(R.id.menu).setOnClickListener {
            dialog.dismiss()
            context.finish()
        }
        dialog.findViewById<Button>(R.id.retry).setOnClickListener {
            reset()
            dialog.dismiss()
        }
        // TODO(jmerm): make next button work.
        val next = dialog.findViewById<Button>(R.id.next)
        next.visibility = View.GONE
        dialog.show()
    }

    fun reset() {
        board = when (params) {
            is RectGameplayParams -> makeRectJBoard(context, params)
            is HexGameplayParams -> makeHexJBoard(context, params)
            is PentGameplayParams -> makePentJBoard(context, params)
            is RingGameplayParams -> makeRingJBoard(context, params)
            is TriangleGameplayParams -> makeTriangleJBoard(context, params)
        }
        undoStack.clear()
        redoStack.clear()
        updateButtons()
        complete = false
    }

    // TODO(jmerm): help should write to the undo stack.
    // TODO(jmerm): fix this when the board is already solved.
    fun help() = board.help()
}
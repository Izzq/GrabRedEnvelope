package com.carlos.grabredenvelope.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var x = -1f
    private var y = -1f
    private var text = ""

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        textSize = 32f
    }

    fun setPoint(x: Float, y: Float, text: String) {
        this.x = x
        this.y = y
        this.text = text
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (x < 0 || y < 0) return

        // 画红点
        canvas.drawCircle(x, y, 8f, pointPaint)

        // 画文字（偏移一点，避免盖住红点）
        canvas.drawText(text, x + 10f, y - 10f, textPaint)
    }
}

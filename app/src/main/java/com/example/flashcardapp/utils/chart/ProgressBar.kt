package com.example.flashcardapp.utils.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.flashcardapp.R

class ProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 85f // 0-100

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = getAttrColor(context, R.attr.iconBlueBackground)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = getAttrColor(context, R.attr.iconBlue)
    }

    private fun getAttrColor(context: Context, attrRes: Int): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attrRes))
        val color = typedArray.getColor(0, 0xFF1F7AE0.toInt())
        typedArray.recycle()
        return color
    }

    private val cornerRadius = 24f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        canvas.drawRoundRect(
            0f, 0f, width, height,
            cornerRadius, cornerRadius,
            trackPaint
        )

        // fill
        val fillWidth = width * (progress / 100f)
        if (fillWidth > 0) {
            canvas.drawRoundRect(
                0f, 0f, fillWidth, height,
                cornerRadius, cornerRadius,
                fillPaint
            )
        }
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f)
        invalidate()
    }

    fun getProgress() = progress
}
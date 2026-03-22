package com.example.flashcardapp.utils.slider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class CustomSliderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var value: Float = 20f        // current
        set(v) { field = v.coerceIn(minValue, maxValue); invalidate() }
    var minValue: Float = 0f
    var maxValue: Float = 100f
    var onValueChanged: ((Float) -> Unit)? = null

    private val trackHeight = dp(8f)
    private val thumbRadius = dp(12f)

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE4E9F1.toInt()
        style = Paint.Style.FILL
    }
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2E6BFF.toInt()
        style = Paint.Style.FILL
    }
    private val thumbStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2E6BFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
    }
    private val thumbFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private val trackRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cxThumb = valueToX(value)
        val centerY = height / 2f

        // inactive track
        trackRect.set(padStart(), centerY - trackHeight / 2, width - padEnd(), centerY + trackHeight / 2)
        canvas.drawRoundRect(trackRect, trackHeight / 2, trackHeight / 2, inactivePaint)

        // active track
        trackRect.right = cxThumb
        canvas.drawRoundRect(trackRect, trackHeight / 2, trackHeight / 2, activePaint)

        // thumb
        canvas.drawCircle(cxThumb, centerY, thumbRadius, thumbFill)
        canvas.drawCircle(cxThumb, centerY, thumbRadius, thumbStroke)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val newVal = xToValue(event.x)
                if (newVal != value) {
                    value = newVal
                    onValueChanged?.invoke(value)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun valueToX(v: Float): Float {
        val t = (v - minValue) / (maxValue - minValue)
        return padStart() + t * (width - padStart() - padEnd())
    }

    private fun xToValue(x: Float): Float {
        val clampedX = min(max(x, padStart()), width - padEnd())
        val t = (clampedX - padStart()) / (width - padStart() - padEnd())
        return minValue + t * (maxValue - minValue)
    }

    private fun padStart() = thumbRadius + dp(4f)
    private fun padEnd() = thumbRadius + dp(4f)
    private fun dp(v: Float) = v * resources.displayMetrics.density
}
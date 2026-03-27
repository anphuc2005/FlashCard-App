package com.example.flashcardapp.core.utils.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.example.flashcardapp.R
import kotlin.math.max

class WeeklyBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class DayEntry(val label: String, val value: Float)

    private var entries: List<DayEntry> = emptyList()
    private var highlightIndex: Int = -1

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.blue_primary) // e.g. #2E6BFF
        style = Paint.Style.FILL
    }
    private val barBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.bar_track)    // e.g. #E5ECF6
        style = Paint.Style.FILL
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.gray_600)     // e.g. #9AA6B6
        textSize = sp(12f)
        textAlign = Paint.Align.CENTER
    }

    private val barRect = RectF()

    private val barWidthDp = 14f
    private val barRadiusDp = 6f
    private val barSpacingDp = 22f
    private val chartTopPaddingDp = 12f
    private val chartBottomPaddingDp = 32f  // space for labels

    fun setData(newEntries: List<DayEntry>, highlight: Int = -1) {
        entries = newEntries
        highlightIndex = highlight
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = dp(140f).toInt()
        val resolvedHeight = resolveSize(desiredHeight, heightMeasureSpec)
        val minWidth =
            (entries.size * (dp(barWidthDp) + dp(barSpacingDp)) + dp(barSpacingDp)).toInt()
        val resolvedWidth = resolveSize(minWidth, widthMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (entries.isEmpty()) return

        val maxVal = max(entries.maxOf { it.value }, 1f)
        val barWidth = dp(barWidthDp)
        val barRadius = dp(barRadiusDp)
        val spacing = dp(barSpacingDp)
        val topPad = dp(chartTopPaddingDp)
        val bottomPad = dp(chartBottomPaddingDp)
        val chartHeight = height - topPad - bottomPad

        var cx = spacing + barWidth / 2f
        entries.forEachIndexed { index, entry ->
            val h = (entry.value / maxVal) * chartHeight
            val barTop = topPad + (chartHeight - h)
            val barBottom = topPad + chartHeight

            // track/background
            barRect.set(cx - barWidth / 2, topPad + chartHeight - dp(4f), cx + barWidth / 2, barBottom)
            canvas.drawRoundRect(barRect, barRadius, barRadius, barBgPaint)

            // bar
            barRect.set(cx - barWidth / 2, barTop, cx + barWidth / 2, barBottom)
            val paint = if (index == highlightIndex) barPaint else barBgPaint.apply { alpha = 200 }
            canvas.drawRoundRect(barRect, barRadius, barRadius, paint)
            paint.alpha = 255 // reset

            // label
            val labelY = height - dp(12f)
            canvas.drawText(entry.label, cx, labelY, labelPaint)

            cx += barWidth + spacing
        }
    }

    private fun dp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics)
    private fun sp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)
}


package com.example.flashcardapp.core.utils.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
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
        color = getAttrColor(R.attr.iconBlue)
        style = Paint.Style.FILL
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = getAttrColor(R.attr.subTitleColor)
        textSize = sp(12f)
        textAlign = Paint.Align.CENTER
    }
    private val activeLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = getAttrColor(R.attr.iconBlue)
        textSize = sp(12f)
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val activeLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = getAttrColor(R.attr.iconBlue)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(3f)
    }

    private val barRect = RectF()

    private val barWidthDp = 14f
    private val barRadiusDp = 6f
    private val minBarHeightDp = 10f
    private val minBarSpacingDp = 14f
    private val maxBarSpacingDp = 30f
    private val horizontalPaddingDp = 18f
    private val chartTopPaddingDp = 10f
    private val chartBottomPaddingDp = 38f

    fun setData(newEntries: List<DayEntry>, highlight: Int = -1) {
        entries = newEntries
        highlightIndex = if (highlight in newEntries.indices) highlight else -1
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = dp(140f).toInt()
        val resolvedHeight = resolveSize(desiredHeight, heightMeasureSpec)
        val minWidth = (
            (entries.size * dp(barWidthDp)) +
                ((entries.size - 1).coerceAtLeast(0) * dp(minBarSpacingDp)) +
                (2 * dp(horizontalPaddingDp))
            ).toInt()
        val resolvedWidth = resolveSize(minWidth, widthMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (entries.isEmpty()) return

        val maxVal = max(entries.maxOf { it.value }, 1f)
        val barWidth = dp(barWidthDp)
        val barRadius = dp(barRadiusDp)
        val minBarHeight = dp(minBarHeightDp)
        val minSpacing = dp(minBarSpacingDp)
        val maxSpacing = dp(maxBarSpacingDp)
        val horizontalPadding = dp(horizontalPaddingDp)
        val topPad = dp(chartTopPaddingDp)
        val bottomPad = dp(chartBottomPaddingDp)
        val chartHeight = height - topPad - bottomPad

        val count = entries.size
        val availableWidth = max(0f, width - (horizontalPadding * 2f))
        val spacing = if (count > 1) {
            ((availableWidth - (count * barWidth)) / (count - 1))
                .coerceIn(minSpacing, maxSpacing)
        } else {
            0f
        }
        val contentWidth = (count * barWidth) + ((count - 1).coerceAtLeast(0) * spacing)
        val startX = ((width - contentWidth) / 2f) + (barWidth / 2f)

        var cx = startX
        entries.forEachIndexed { index, entry ->
            val rawHeight = (entry.value / maxVal) * chartHeight
            val displayHeight = if (entry.value > 0f) max(rawHeight, minBarHeight) else 0f
            val barTop = topPad + (chartHeight - displayHeight)
            val barBottom = topPad + chartHeight

            if (displayHeight > 0f) {
                barRect.set(cx - barWidth / 2, barTop, cx + barWidth / 2, barBottom)
                val oldAlpha = barPaint.alpha
                if (index != highlightIndex) barPaint.alpha = 110
                canvas.drawRoundRect(barRect, barRadius, barRadius, barPaint)
                barPaint.alpha = oldAlpha
            }

            if (index == highlightIndex) {
                val lineWidth = dp(30f)
                val lineY = height - bottomPad + dp(6f)
                canvas.drawLine(cx - lineWidth / 2f, lineY, cx + lineWidth / 2f, lineY, activeLinePaint)
            }

            val labelY = height - dp(10f)
            val paint = if (index == highlightIndex) activeLabelPaint else labelPaint
            canvas.drawText(entry.label, cx, labelY, paint)

            cx += barWidth + spacing
        }
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
    }

    private fun sp(value: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)
    }

    private fun getAttrColor(attrRes: Int): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attrRes))
        val color = typedArray.getColor(0, 0xFF1F7AE0.toInt())
        typedArray.recycle()
        return color
    }
}

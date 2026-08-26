package org.pelmeshke.nulldex.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.pelmeshke.nulldex.R
import org.pelmeshke.nulldex.ui.theme.PokemonTypeColors

class PokemonTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        alpha = 90
        strokeWidth = 2f * resources.displayMetrics.density
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.text_s)
        isFakeBoldText = true
    }

    var typeName: String = ""
        set(value) {
            field = value
            paint.color = PokemonTypeColors.getColor(context, value)
            invalidate()
            requestLayout()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val label = displayLabel()
        val horizontalPad = (14 * resources.displayMetrics.density).toInt()
        val desiredWidth = (textPaint.measureText(label) + horizontalPad * 2).toInt()
        val desiredHeight = resources.getDimensionPixelSize(R.dimen.type_badge_height)
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = height / 2f
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
        val inset = borderPaint.strokeWidth / 2f
        canvas.drawRoundRect(
            inset,
            inset,
            width.toFloat() - inset,
            height.toFloat() - inset,
            radius,
            radius,
            borderPaint
        )
        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(displayLabel(), x, y, textPaint)
    }

    private fun displayLabel(): String =
        typeName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

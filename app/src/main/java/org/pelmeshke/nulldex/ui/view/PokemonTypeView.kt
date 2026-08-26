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

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 36f
    }

    init {
        borderPaint.color = resources.getColor(R.color.gray_primary)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
    }

    var typeName: String = ""
        set(value) {
            field = value
            paint.color = getTypeColor(value)
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = height / 2f
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
        canvas.drawRoundRect(
            borderPaint.strokeWidth / 2,
            borderPaint.strokeWidth / 2,
            width.toFloat() - borderPaint.strokeWidth / 2,
            height.toFloat() - borderPaint.strokeWidth / 2,
            radius,
            radius,
            borderPaint
        )
        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(typeName.replaceFirstChar { it.uppercase() }, x, y, textPaint)
        canvas.drawText(typeName.replaceFirstChar { it.uppercase() }, x, y, textPaint)
    }

    private fun getTypeColor(type: String): Int =
        PokemonTypeColors.getColor(context, type)
}
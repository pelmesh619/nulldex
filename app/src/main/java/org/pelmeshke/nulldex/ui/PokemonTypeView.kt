package org.pelmeshke.nulldex.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class PokemonTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 36f
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
        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(typeName.replaceFirstChar { it.uppercase() }, x, y, textPaint)
    }

    private fun getTypeColor(type: String): Int = when (type.lowercase()) {
        "grass"    -> "#78C850".toColorInt()
        "fire"     -> "#F08030".toColorInt()
        "water"    -> "#6890F0".toColorInt()
        "electric" -> "#F8D030".toColorInt()
        "psychic"  -> "#F85888".toColorInt()
        "ice"      -> "#98D8D8".toColorInt()
        "dragon"   -> "#7038F8".toColorInt()
        "dark"     -> "#705848".toColorInt()
        "fairy"    -> "#EE99AC".toColorInt()
        "fighting" -> "#C03028".toColorInt()
        "flying"   -> "#A890F0".toColorInt()
        "poison"   -> "#A040A0".toColorInt()
        "ground"   -> "#E0C068".toColorInt()
        "rock"     -> "#B8A038".toColorInt()
        "bug"      -> "#A8B820".toColorInt()
        "ghost"    -> "#705898".toColorInt()
        "steel"    -> "#B8B8D0".toColorInt()
        "normal"   -> "#A8A878".toColorInt()
        "stellar"  -> "#40b5a5".toColorInt()
        else       -> Color.GRAY
    }
}
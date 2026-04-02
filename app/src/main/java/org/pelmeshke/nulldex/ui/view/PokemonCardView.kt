package org.pelmeshke.nulldex.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

class PokemonCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

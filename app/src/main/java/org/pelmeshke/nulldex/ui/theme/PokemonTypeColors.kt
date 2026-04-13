package org.pelmeshke.nulldex.ui.theme

import android.content.Context
import androidx.core.content.ContextCompat
import org.pelmeshke.nulldex.R

class PokemonTypeColors {
    companion object {
        fun getColorRes(type: String): Int = when (type.lowercase()) {
            "grass" -> R.color.type_grass
            "fire" -> R.color.type_fire
            "water" -> R.color.type_water
            "electric" -> R.color.type_electric
            "psychic" -> R.color.type_psychic
            "ice" -> R.color.type_ice
            "dragon" -> R.color.type_dragon
            "dark" -> R.color.type_dark
            "fairy" -> R.color.type_fairy
            "fighting" -> R.color.type_fighting
            "flying" -> R.color.type_flying
            "poison" -> R.color.type_poison
            "ground" -> R.color.type_ground
            "rock" -> R.color.type_rock
            "bug" -> R.color.type_bug
            "ghost" -> R.color.type_ghost
            "steel" -> R.color.type_steel
            else -> R.color.type_normal
        }

        fun getColor(context: Context, type: String): Int =
            ContextCompat.getColor(context, getColorRes(type))
    }
}
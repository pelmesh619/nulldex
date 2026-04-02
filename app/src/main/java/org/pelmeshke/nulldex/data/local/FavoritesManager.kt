package org.pelmeshke.nulldex.data.local

import android.content.Context
import org.pelmeshke.nulldex.data.model.PokemonEntry

class FavoritesManager(context: Context) {
    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun add(name: String, id: String) = prefs.edit()
        .putString(name, "https://pokeapi.co/api/v2/pokemon/$id/")
        .apply()

    fun remove(name: String) = prefs.edit().remove(name).apply()

    fun isFavorite(name: String) = prefs.contains(name)

    fun getAll(): List<PokemonEntry> = prefs.all
        .filter { (_, value) -> value is String }
        .map { (name, url) ->
            PokemonEntry(name = name, url = url as String)
        }
}
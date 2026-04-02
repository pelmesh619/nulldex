package org.pelmeshke.nulldex.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.pelmeshke.nulldex.data.local.FavoritesManager
import org.pelmeshke.nulldex.data.model.PokemonEntry

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application)

    private val _favorites = MutableLiveData<List<PokemonEntry>>()
    val favorites: LiveData<List<PokemonEntry>> = _favorites

    fun loadFavorites() {
        _favorites.value = favoritesManager.getAll()
    }
}
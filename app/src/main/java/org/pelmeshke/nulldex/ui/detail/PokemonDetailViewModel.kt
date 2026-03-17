package org.pelmeshke.nulldex.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pelmeshke.nulldex.data.model.Pokemon
import org.pelmeshke.nulldex.repository.PokemonRepository

class PokemonDetailViewModel : ViewModel() {
    private val repository = PokemonRepository()

    private val _pokemon = MutableLiveData<Pokemon>()
    val pokemon: LiveData<Pokemon> = _pokemon

    fun loadPokemon(name: String) {
        viewModelScope.launch {
            try {
                _pokemon.value = repository.getPokemon(name)
            } catch (e: Exception) {
                // TODO
            }
        }
    }
}

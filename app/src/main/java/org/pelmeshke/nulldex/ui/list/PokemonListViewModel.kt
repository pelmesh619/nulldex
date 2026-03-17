package org.pelmeshke.nulldex.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pelmeshke.nulldex.data.model.PokemonEntry
import org.pelmeshke.nulldex.repository.PokemonRepository

class PokemonListViewModel : ViewModel() {
    private val repository = PokemonRepository()

    private val _pokemonList = MutableLiveData<List<PokemonEntry>>()
    val pokemonList: LiveData<List<PokemonEntry>> = _pokemonList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadPokemonList()
    }

    private fun loadPokemonList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPokemonList()
                _pokemonList.value = result.results
            } catch (e: Exception) {
                // TODO
            } finally {
                _isLoading.value = false
            }
        }
    }
}

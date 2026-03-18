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

    private var currentOffset = 0
    private val pageSize = 20
    private var isLastPage = false
    private var isCurrentlyLoading = false

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isCurrentlyLoading || isLastPage) return

        viewModelScope.launch {
            isCurrentlyLoading = true
            _isLoading.value = true
            try {
                val result = repository.getPokemonList(pageSize, currentOffset)
                val current = _pokemonList.value.orEmpty()
                _pokemonList.value = current + result.results
                currentOffset += pageSize
                if (result.results.size < pageSize) isLastPage = true
            } catch (e: Exception) {
                // TODO
            } finally {
                _isLoading.value = false
                isCurrentlyLoading = false
            }
        }
    }
}

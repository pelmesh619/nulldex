package org.pelmeshke.nulldex.ui.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pelmeshke.nulldex.data.model.PokemonEntry
import org.pelmeshke.nulldex.repository.PokemonRepository
import retrofit2.HttpException
import java.io.IOException

class PokemonListViewModel : ViewModel() {
    private val repository = PokemonRepository()

    private var allPokemons: List<PokemonEntry> = emptyList()

    private val _pokemonList = MutableLiveData<List<PokemonEntry>>()
    val pokemonList: LiveData<List<PokemonEntry>> = _pokemonList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentOffset = 0
    private val pageSize = 20
    private var isLastPage = false
    private var isCurrentlyLoading = false
    private var searchQuery = ""
    var lastQuery: String = ""

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAllPokemons()
    }

    fun loadAllPokemons() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPokemonList(limit = 100000, offset = 0)
                allPokemons = result.results
                applySearch()
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.code()}"
                Log.e(null, e.toString())
            } catch (e: IOException) {
                _error.value = "No connection"
                Log.e(null, e.toString())
            } catch (e: Exception) {
                _error.value = "Other error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun search(query: String) {
        searchQuery = query
        if (query.isNotEmpty()) {
            lastQuery = query
        }
        applySearch()
    }

    private fun applySearch() {
        _pokemonList.value = if (searchQuery.isEmpty()) {
            allPokemons.take(currentOffset.coerceAtLeast(pageSize))
        } else {
            allPokemons.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    fun loadNextPage() {
        if (searchQuery.isNotEmpty()) return
        if (isCurrentlyLoading || isLastPage) return

        val currentSize = _pokemonList.value?.size ?: 0
        val nextPage = allPokemons.drop(currentSize).take(pageSize)
        if (nextPage.isEmpty()) {
            isLastPage = true
            return
        }
        _pokemonList.value = _pokemonList.value.orEmpty() + nextPage
    }

    fun clearError() {
        _error.value = null
    }
}
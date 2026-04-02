package org.pelmeshke.nulldex.ui.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.pelmeshke.nulldex.data.model.PokemonEntry
import org.pelmeshke.nulldex.data.repository.PokemonRepository
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.text.padStart

class PokemonListViewModel : ViewModel() {
    private val repository = PokemonRepository()

    private var allPokemons: List<PokemonEntry> = emptyList()

    private val _pokemonList = MutableLiveData<List<PokemonEntry>>()
    val pokemonList: LiveData<List<PokemonEntry>> = _pokemonList

    private val _isListVisible = MutableLiveData(true)
    val isListVisible: LiveData<Boolean> = _isListVisible

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

    private var loadJob: Job? = null
    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    init {
        loadAllPokemons()
    }

    fun refresh() {
        Log.d("PokemonListViewModel", "Refreshing... ${loadJob?.isActive} ${loadJob?.isCompleted} ${loadJob?.isCancelled}")
        loadJob?.cancel(CancellationException())
        Log.d("PokemonListViewModel", "Refreshing 2... ${loadJob?.isActive} ${loadJob?.isCompleted} ${loadJob?.isCancelled}")
        _isRefreshing.value = true
        _isListVisible.value = false
        loadJob = viewModelScope.launch {
            try {
                loadAllPokemonsSuspending()
                applySearch()
            } catch (e: CancellationException) {
                Log.e("PokemonListViewModel", "Refresh was canceled")
                throw e
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadAllPokemonsSuspending() {
        _isLoading.value = true
        try {
            delay(7000)
            val result = repository.getPokemonList(limit = 100000, offset = 0)
            allPokemons = result.results
            _error.value = null
        } catch (e: HttpException) {
            _error.value = "Server error: ${e.code()}"
            Log.e("PokemonListVM", "HttpException", e)
        } catch (e: IOException) {
            _error.value = "No connection"
            Log.e("PokemonListVM", "IOException", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _error.value = "Other error"
            Log.e("PokemonListVM", "Exception", e)
        } finally {
            _isLoading.value = false
            _isListVisible.value = true
        }
    }

    fun loadAllPokemons() {
        viewModelScope.launch {
            _isLoading.value = true
            _isListVisible.value = false
            try {
                val result = repository.getPokemonList(limit = 100000, offset = 0)
                allPokemons = result.results
                applySearch()
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.code()}"
                Log.e("PokemonListViewModel", e.toString())
            } catch (e: IOException) {
                _error.value = "No connection"
                Log.e("PokemonListViewModel", e.toString())
            } catch (e: Exception) {
                _error.value = "Other error"
                Log.e("PokemonListViewModel", e.toString())
            } finally {
                _isLoading.value = false
                _isListVisible.value = true
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
            allPokemons.withIndex().filter {
                it.value.name.contains(searchQuery, ignoreCase = true) or
                        ("#" + (it.index + 1).toString().padStart(3, '0')).contains(searchQuery)
            }.map { it.value }
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
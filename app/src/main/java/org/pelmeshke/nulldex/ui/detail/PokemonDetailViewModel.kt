package org.pelmeshke.nulldex.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pelmeshke.nulldex.data.model.Pokemon
import org.pelmeshke.nulldex.repository.PokemonRepository
import retrofit2.HttpException
import java.io.IOException

class PokemonDetailViewModel : ViewModel() {
    private val repository = PokemonRepository()

    private val _pokemon = MutableLiveData<Pokemon>()
    val pokemon: LiveData<Pokemon> = _pokemon

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPokemon(name: String) {
        viewModelScope.launch {
            try {
                _pokemon.value = repository.getPokemon(name)
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.code()}"
                Log.e(null, e.toString())
            } catch (e: IOException) {
                _error.value = "No connection"
                Log.e(null, e.toString())
            } catch (e: Exception) {
                _error.value = "Other error"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

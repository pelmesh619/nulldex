package org.pelmeshke.nulldex.ui.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pelmeshke.nulldex.data.model.Pokemon
import org.pelmeshke.nulldex.data.model.PokemonUIConfig
import org.pelmeshke.nulldex.data.repository.PokemonRepository
import org.pelmeshke.nulldex.data.sdui.UIConfigStore
import retrofit2.HttpException
import java.io.IOException

class PokemonDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PokemonRepository()
    private val uiConfigStore = UIConfigStore(application)

    private val _pokemon = MutableLiveData<Pokemon>()
    val pokemon: LiveData<Pokemon> = _pokemon

    private val _uiConfig = MutableLiveData<PokemonUIConfig>()
    val uiConfig: LiveData<PokemonUIConfig> = _uiConfig

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPokemon(name: String) {
        viewModelScope.launch {
            try {
                _pokemon.value = repository.getPokemon(name)
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.code()}"
                Log.e(TAG, e.toString())
            } catch (e: IOException) {
                _error.value = "No connection"
                Log.e(TAG, e.toString())
            } catch (e: Exception) {
                _error.value = "Other error"
                Log.e(TAG, e.toString())
            }
        }
    }

    fun loadUIConfig() {
        viewModelScope.launch {
            try {
                _uiConfig.value = uiConfigStore.load()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load SDUI config", e)
                _uiConfig.value = PokemonUIConfig(emptyList())
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        private const val TAG = "SDUI"
    }
}

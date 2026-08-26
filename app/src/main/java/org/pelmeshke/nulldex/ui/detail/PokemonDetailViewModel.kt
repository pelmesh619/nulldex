package org.pelmeshke.nulldex.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pelmeshke.nulldex.data.model.Pokemon
import org.pelmeshke.nulldex.data.model.PokemonUIConfig
import org.pelmeshke.nulldex.data.model.UIActionConfig
import org.pelmeshke.nulldex.data.model.UIAnalyticsConfig
import org.pelmeshke.nulldex.data.model.UIComponentConfig
import org.pelmeshke.nulldex.data.repository.PokemonRepository
import retrofit2.HttpException
import java.io.IOException

class PokemonDetailViewModel : ViewModel() {
    private val repository = PokemonRepository()

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
                Log.e(null, e.toString())
            } catch (e: IOException) {
                _error.value = "No connection"
                Log.e(null, e.toString())
            } catch (e: Exception) {
                _error.value = "Other error"
            }
        }
    }

    fun loadUIConfig() {
        viewModelScope.launch {
            try {
                _uiConfig.value = repository.getPokemonUIConfig()
            } catch (e: Exception) {
                Log.e("SDUI", "Error while loading UI config: ${e.toString()}")
                _uiConfig.value = defaultUIConfig()
            }
        }
    }

    private fun defaultUIConfig() = PokemonUIConfig(
        components = listOf(
            UIComponentConfig(
                id = "sprite",
                type = "sprite",
                analytics = UIAnalyticsConfig(impressionEvent = "pokemon_sprite_impression")
            ),
            UIComponentConfig(
                id = "number",
                type = "number",
                analytics = UIAnalyticsConfig(impressionEvent = "pokemon_number_impression")
            ),
            UIComponentConfig(
                id = "title",
                type = "title",
                action = UIActionConfig("show_toast", mapOf("message" to "Pokemon title tapped")),
            ),
            UIComponentConfig(
                id = "types",
                type = "type_badges",
                analytics = UIAnalyticsConfig(impressionEvent = "pokemon_types_impression")
            ),
            UIComponentConfig(
                id = "divider",
                type = "divider"
            ),
            UIComponentConfig(
                id = "height",
                type = "stat",
                label = "Height",
                analytics = UIAnalyticsConfig(impressionEvent = "pokemon_height_impression")
            ),
            UIComponentConfig(
                id = "weight",
                type = "stat",
                label = "Weight",
                analytics = UIAnalyticsConfig(impressionEvent = "pokemon_weight_impression")
            ),
            UIComponentConfig(
                id = "base_experience",
                type = "stat",
                label = "Base experience",
                action = UIActionConfig("show_toast", mapOf("message" to "Base experience tapped")),
                analytics = UIAnalyticsConfig(
                    impressionEvent = "pokemon_base_experience_impression"
                )
            ),
        )
    )

    fun clearError() {
        _error.value = null
    }
}

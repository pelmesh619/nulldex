package org.pelmeshke.nulldex.data.sdui

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pelmeshke.nulldex.BuildConfig
import org.pelmeshke.nulldex.data.api.RetrofitInstance
import org.pelmeshke.nulldex.data.model.PokemonUIConfig

class UIConfigStore(
    context: Context,
    private val gson: Gson = Gson()
) {
    private val appContext = context.applicationContext

    suspend fun load(): PokemonUIConfig = withContext(Dispatchers.IO) {
        val local = loadBundled()
        if (!BuildConfig.SDUI_REMOTE_ENABLED) {
            return@withContext local
        }
        try {
            RetrofitInstance.uiApi.getPokemonUIConfig()
        } catch (e: Exception) {
            Log.w(TAG, "Remote SDUI unavailable, using bundled config", e)
            local
        }
    }

    private fun loadBundled(): PokemonUIConfig {
        return appContext.assets.open(ASSET_PATH).bufferedReader().use { reader ->
            gson.fromJson(reader, PokemonUIConfig::class.java)
                ?: PokemonUIConfig(emptyList())
        }
    }

    companion object {
        private const val TAG = "SDUI"
        private const val ASSET_PATH = "sdui/pokemon_ui.json"
    }
}

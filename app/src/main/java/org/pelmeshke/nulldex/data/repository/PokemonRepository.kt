package org.pelmeshke.nulldex.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pelmeshke.nulldex.data.api.RetrofitInstance
import org.pelmeshke.nulldex.data.model.AnalyticsEvent
import org.pelmeshke.nulldex.data.model.Pokemon
import org.pelmeshke.nulldex.data.model.PokemonList
import retrofit2.HttpException
import java.io.IOException

class PokemonRepository {
    private val api = RetrofitInstance.api

    suspend fun getPokemonList(
        limit: Int = 20,
        offset: Int = 0,
        forceRefresh: Boolean = false
    ): PokemonList = withContext(Dispatchers.IO) {
        runCatchingCacheMiss {
            api.getPokemonList(limit, offset, cacheControl(forceRefresh))
        }
    }

    suspend fun getPokemon(
        name: String,
        forceRefresh: Boolean = false
    ): Pokemon = withContext(Dispatchers.IO) {
        runCatchingCacheMiss {
            api.getPokemon(name, cacheControl(forceRefresh))
        }
    }

    suspend fun sendAnalyticsEvent(event: AnalyticsEvent) =
        RetrofitInstance.analyticsApi.sendEvent(event)

    private fun cacheControl(forceRefresh: Boolean): String? =
        if (forceRefresh) "no-cache" else null

    private inline fun <T> runCatchingCacheMiss(block: () -> T): T {
        try {
            return block()
        } catch (e: HttpException) {
            if (e.code() == HTTP_CACHE_UNSATISFIABLE) {
                throw IOException("No connection", e)
            }
            throw e
        }
    }

    private companion object {
        const val HTTP_CACHE_UNSATISFIABLE = 504
    }
}

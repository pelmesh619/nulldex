package org.pelmeshke.nulldex.data.repository

import org.pelmeshke.nulldex.data.model.AnalyticsEvent
import org.pelmeshke.nulldex.data.api.RetrofitInstance

class PokemonRepository {
    private val api = RetrofitInstance.api

    suspend fun getPokemonList(limit: Int = 20, offset: Int = 0) =
        api.getPokemonList(limit, offset)

    suspend fun getPokemon(name: String) =
        api.getPokemon(name)

    suspend fun getPokemonUIConfig() = RetrofitInstance.uiApi.getPokemonUIConfig()

    suspend fun sendAnalyticsEvent(event: AnalyticsEvent) =
        RetrofitInstance.analyticsApi.sendEvent(event)
}

package org.pelmeshke.nulldex.data.api

import org.pelmeshke.nulldex.data.model.Pokemon
import org.pelmeshke.nulldex.data.model.PokemonList
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Header("Cache-Control") cacheControl: String? = null
    ): PokemonList

    @GET("pokemon/{name}")
    suspend fun getPokemon(
        @Path("name") name: String,
        @Header("Cache-Control") cacheControl: String? = null
    ): Pokemon
}

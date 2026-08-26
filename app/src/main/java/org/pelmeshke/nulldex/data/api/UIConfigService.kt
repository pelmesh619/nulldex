package org.pelmeshke.nulldex.data.api

import org.pelmeshke.nulldex.data.model.PokemonUIConfig
import retrofit2.http.GET

interface UIConfigService {
    @GET("ui/pokemon")
    suspend fun getPokemonUIConfig(): PokemonUIConfig
}
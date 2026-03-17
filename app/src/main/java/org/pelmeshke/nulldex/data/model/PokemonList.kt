package org.pelmeshke.nulldex.data.model

data class PokemonList(
    val count: Int,
    val results: List<PokemonEntry>
)

data class PokemonEntry(
    val name: String,
    val url: String
)

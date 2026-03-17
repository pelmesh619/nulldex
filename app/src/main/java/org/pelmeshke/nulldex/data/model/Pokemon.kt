package org.pelmeshke.nulldex.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val types: List<TypeSlot>
)

data class Sprites(
    val frontDefault: String
)

data class TypeSlot(
    val type: Type
)

data class Type(
    val name: String
)

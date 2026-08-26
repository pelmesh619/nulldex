package org.pelmeshke.nulldex.data.model

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val types: List<TypeSlot>,
    val abilities: List<AbilitySlot>,
    @SerializedName("base_experience")
    val baseExperience: Int,
)

data class Sprites(
    @SerializedName("front_default")
    val frontDefault: String?
)

data class TypeSlot(
    val type: Type
)

data class Type(
    val name: String
)

data class AbilitySlot(
    val ability: Ability,
    @SerializedName("is_hidden")
    val isHidden: Boolean
)

data class Ability(
    val name: String
)

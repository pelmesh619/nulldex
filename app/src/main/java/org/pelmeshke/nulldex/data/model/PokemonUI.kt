package org.pelmeshke.nulldex.data.model

sealed class PokemonUIComponent {
    data class Sprite(
        val id: String,
        val url: String,
        val action: UIActionConfig? = null,
        val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Title(
        val id: String,
        val text: String,
        val action: UIActionConfig? = null,
        val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Number(
        val id: String,
        val text: String,
        val action: UIActionConfig? = null,
        val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class TypeBadges(
        val id: String,
        val types: List<String>,
        val action: UIActionConfig? = null,
        val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Abilities(
        val id: String,
        val title: String,
        val abilities: List<String>,
        val action: UIActionConfig? = null,
        val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Stat(
        val id: String,
        val label: String,
        val value: String,
        val action: UIActionConfig? = null,
        val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Divider(
        val id: String,
        val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()
}

data class PokemonUIModel(
    val primaryColor: Int,
    val components: List<PokemonUIComponent>
)

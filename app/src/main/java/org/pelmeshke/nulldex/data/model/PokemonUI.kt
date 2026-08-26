package org.pelmeshke.nulldex.data.model

sealed class PokemonUIComponent {
    abstract val id: String
    open val action: UIActionConfig? get() = null
    open val analytics: UIAnalyticsConfig? get() = null

    data class Sprite(
        override val id: String,
        val url: String,
        override val action: UIActionConfig? = null,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Title(
        override val id: String,
        val text: String,
        override val action: UIActionConfig? = null,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Number(
        override val id: String,
        val text: String,
        override val action: UIActionConfig? = null,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class TypeBadges(
        override val id: String,
        val types: List<String>,
        override val action: UIActionConfig? = null,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Abilities(
        override val id: String,
        val title: String,
        val abilities: List<String>,
        override val action: UIActionConfig? = null,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Stat(
        override val id: String,
        val label: String,
        val value: String,
        override val action: UIActionConfig? = null,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Divider(
        override val id: String,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Section(
        override val id: String,
        val title: String,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()

    data class Button(
        override val id: String,
        val label: String,
        override val action: UIActionConfig? = null,
        override val analytics: UIAnalyticsConfig? = null
    ) : PokemonUIComponent()
}

data class PokemonUIModel(
    val primaryColor: Int,
    val components: List<PokemonUIComponent>
)

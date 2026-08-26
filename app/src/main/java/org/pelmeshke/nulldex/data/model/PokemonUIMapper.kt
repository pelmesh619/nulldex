package org.pelmeshke.nulldex.data.model

import android.content.Context
import org.pelmeshke.nulldex.ui.theme.PokemonTypeColors

object PokemonUIMapper {
    fun map(pokemon: Pokemon, config: PokemonUIConfig, context: Context): PokemonUIModel {
        val primaryType = pokemon.types.firstOrNull()?.type?.name ?: "normal"
        val primaryColor = PokemonTypeColors.getColor(context, primaryType)

        val components = config.components.mapNotNull { component ->
            when (component.type) {
                "sprite" -> PokemonUIComponent.Sprite(
                    id = component.id,
                    url = pokemon.sprites.bestImageUrl,
                    action = component.action,
                    analytics = component.analytics
                )
                "number" -> PokemonUIComponent.Number(
                    id = component.id,
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    action = component.action,
                    analytics = component.analytics
                )
                "title" -> PokemonUIComponent.Title(
                    id = component.id,
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    action = component.action,
                    analytics = component.analytics
                )
                "type_badges" -> PokemonUIComponent.TypeBadges(
                    id = component.id,
                    types = pokemon.types.map { it.type.name },
                    action = component.action,
                    analytics = component.analytics
                )
                "abilities" -> PokemonUIComponent.Abilities(
                    id = component.id,
                    title = component.label ?: "Abilities",
                    abilities = pokemon.abilities.map { slot ->
                        buildString {
                            append(
                                slot.ability.name
                                    .replace("-", " ")
                                    .replaceFirstChar { it.uppercase() }
                            )
                            if (slot.isHidden) append(" · Hidden")
                        }
                    },
                    action = component.action,
                    analytics = component.analytics
                )
                "divider" -> PokemonUIComponent.Divider(
                    id = component.id,
                    analytics = component.analytics
                )
                "section" -> PokemonUIComponent.Section(
                    id = component.id,
                    title = component.label ?: "",
                    analytics = component.analytics
                )
                "button" -> PokemonUIComponent.Button(
                    id = component.id,
                    label = component.label ?: "Action",
                    action = component.action,
                    analytics = component.analytics
                )
                "stat" -> mapStat(pokemon, component)
                else -> null
            }
        }

        return PokemonUIModel(primaryColor, components)
    }

    private fun mapStat(
        pokemon: Pokemon,
        component: UIComponentConfig
    ): PokemonUIComponent.Stat? {
        val key = component.label?.lowercase().orEmpty()
        val (label, value) = when {
            key.contains("height") -> "Height" to "${pokemon.height / 10.0} m"
            key.contains("weight") -> "Weight" to "${pokemon.weight / 10.0} kg"
            key.contains("exp") || key.contains("experience") ->
                "Base EXP" to "${pokemon.baseExperience}"
            else -> return null
        }
        return PokemonUIComponent.Stat(
            id = component.id,
            label = label,
            value = value,
            action = component.action,
            analytics = component.analytics
        )
    }
}

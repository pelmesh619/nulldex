package org.pelmeshke.nulldex.data.model

data class UIActionConfig(
    val type: String = "",
    val payload: Map<String, String>? = emptyMap()
) {
    fun payloadOrEmpty(): Map<String, String> = payload.orEmpty()
}

data class UIAnalyticsConfig(
    val impressionEvent: String? = null,
    val clickEvent: String? = null,
    val params: Map<String, String> = emptyMap()
)

data class UIComponentConfig(
    val id: String,
    val type: String,
    val label: String? = null,
    val action: UIActionConfig? = null,
    val analytics: UIAnalyticsConfig? = null
)

data class PokemonUIConfig(
    val components: List<UIComponentConfig>
)

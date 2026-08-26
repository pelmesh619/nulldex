package org.pelmeshke.nulldex.ui.detail

import org.pelmeshke.nulldex.data.model.UIActionConfig

fun interface PokemonActionHandler {
    fun handle(componentId: String, action: UIActionConfig)
}

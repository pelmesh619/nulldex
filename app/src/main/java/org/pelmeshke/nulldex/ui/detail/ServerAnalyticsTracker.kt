package org.pelmeshke.nulldex.ui.detail

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.pelmeshke.nulldex.data.model.AnalyticsEvent
import org.pelmeshke.nulldex.data.repository.PokemonRepository

class ServerAnalyticsTracker(
    private val repository: PokemonRepository = PokemonRepository()
) : AnalyticsTracker {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun track(event: String, params: Map<String, String>) {
        val analyticsEvent = AnalyticsEvent(event = event, params = params)

        scope.launch {
            try {
                repository.sendAnalyticsEvent(analyticsEvent)
            } catch (e: Exception) {
                Log.w(
                    "SDUI_ANALYTICS",
                    "Failed to send event=${analyticsEvent.event} params=${analyticsEvent.params}: $e"
                )
            }
        }
    }
}

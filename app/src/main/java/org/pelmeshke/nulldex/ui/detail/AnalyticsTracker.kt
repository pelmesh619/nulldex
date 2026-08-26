package org.pelmeshke.nulldex.ui.detail

import android.util.Log
import org.pelmeshke.nulldex.BuildConfig

interface AnalyticsTracker {
    fun track(event: String, params: Map<String, String> = emptyMap())
}

class LogAnalyticsTracker : AnalyticsTracker {
    override fun track(event: String, params: Map<String, String>) {
        Log.d(TAG, "$event $params")
    }

    companion object {
        private const val TAG = "SDUI"
    }
}

class CompositeAnalyticsTracker(
    private val delegates: List<AnalyticsTracker>
) : AnalyticsTracker {
    override fun track(event: String, params: Map<String, String>) {
        delegates.forEach { it.track(event, params) }
    }
}

object AnalyticsTrackers {
    fun create(): AnalyticsTracker {
        val local = LogAnalyticsTracker()
        return if (BuildConfig.SDUI_REMOTE_ENABLED) {
            CompositeAnalyticsTracker(listOf(local, ServerAnalyticsTracker()))
        } else {
            local
        }
    }
}

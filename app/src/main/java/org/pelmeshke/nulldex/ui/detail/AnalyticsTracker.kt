package org.pelmeshke.nulldex.ui.detail

interface AnalyticsTracker {
    fun track(event: String, params: Map<String, String> = emptyMap())
}

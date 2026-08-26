package org.pelmeshke.nulldex.data.model

data class AnalyticsEvent(
    val event: String,
    val params: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

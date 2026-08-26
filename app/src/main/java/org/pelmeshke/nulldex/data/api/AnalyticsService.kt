package org.pelmeshke.nulldex.data.api

import org.pelmeshke.nulldex.data.model.AnalyticsEvent
import retrofit2.http.Body
import retrofit2.http.POST

interface AnalyticsService {
    @POST("analytics/events")
    suspend fun sendEvent(@Body event: AnalyticsEvent)
}

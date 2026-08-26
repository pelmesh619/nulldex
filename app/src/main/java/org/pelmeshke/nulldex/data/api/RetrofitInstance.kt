package org.pelmeshke.nulldex.data.api

import okhttp3.OkHttpClient
import org.pelmeshke.nulldex.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"
    private const val UI_BASE_URL = BuildConfig.UI_BASE_URL

    private val uiClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(200, TimeUnit.MILLISECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: PokeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }

    val uiApi: UIConfigService by lazy {
        Retrofit.Builder()
            .baseUrl(UI_BASE_URL)
            .client(uiClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UIConfigService::class.java)
    }

    val analyticsApi: AnalyticsService by lazy {
        Retrofit.Builder()
            .baseUrl(UI_BASE_URL)
            .client(uiClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnalyticsService::class.java)
    }
}

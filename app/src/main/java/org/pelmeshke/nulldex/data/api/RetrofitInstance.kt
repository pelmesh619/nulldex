package org.pelmeshke.nulldex.data.api

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.pelmeshke.nulldex.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    private val uiClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private lateinit var pokeClient: OkHttpClient

    fun init(context: Context) {
        if (::pokeClient.isInitialized) return
        val appContext = context.applicationContext
        pokeClient = OkHttpClient.Builder()
            .cache(Cache(File(appContext.cacheDir, HTTP_CACHE_DIR), HTTP_CACHE_SIZE_BYTES))
            .addInterceptor(OfflineCacheInterceptor(appContext))
            .addNetworkInterceptor(CacheRewriteInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val api: PokeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(pokeClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }

    val uiApi: UIConfigService by lazy {
        uiRetrofit.create(UIConfigService::class.java)
    }

    val analyticsApi: AnalyticsService by lazy {
        uiRetrofit.create(AnalyticsService::class.java)
    }

    private val uiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.UI_BASE_URL)
            .client(uiClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

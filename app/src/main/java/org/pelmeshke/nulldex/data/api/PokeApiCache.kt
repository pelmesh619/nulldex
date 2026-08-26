package org.pelmeshke.nulldex.data.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

internal class OfflineCacheInterceptor(
    context: Context
) : Interceptor {
    private val appContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!appContext.isNetworkAvailable()) {
            request = request.newBuilder()
                .cacheControl(
                    CacheControl.Builder()
                        .maxStale(CACHE_STALE_DAYS, TimeUnit.DAYS)
                        .onlyIfCached()
                        .build()
                )
                .build()
        }
        return chain.proceed(request)
    }
}

internal class CacheRewriteInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code != 200) return response
        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", "public, max-age=$CACHE_MAX_AGE_SECONDS")
            .build()
    }
}

internal const val HTTP_CACHE_DIR = "http_pokeapi"
internal const val HTTP_CACHE_SIZE_BYTES = 20L * 1024 * 1024
private const val CACHE_MAX_AGE_SECONDS = 60 * 60 * 24
private const val CACHE_STALE_DAYS = 7

private fun Context.isNetworkAvailable(): Boolean {
    val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivity.activeNetwork ?: return false
    val capabilities = connectivity.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

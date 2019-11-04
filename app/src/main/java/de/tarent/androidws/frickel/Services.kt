package de.tarent.androidws.frickel

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ServiceCreator {

    private const val CONNECT_TIME_OUT = 5L
    private const val CALL_TIME_OUT = 5L
    private const val READ_TIME_OUT = 10L
    private const val WRITE_TIME_OUT = 10L
    private const val CACHE_SIZE_BYTES = 1024 * 1024 * 10L

    operator fun <T> invoke(context: Context, baseUrl: String, kClass: Class<out T>): T {

        fun cache() = Cache(context.cacheDir, CACHE_SIZE_BYTES)

        fun httpLogger() = object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("RestoHttp", message)
            }
        }

        fun loggingInterceptor() = HttpLoggingInterceptor(httpLogger()).apply { level = HttpLoggingInterceptor.Level.BODY }

        fun okHttpClient() = OkHttpClient.Builder()
                .apply {
                    connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                    readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                    writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                    callTimeout(CALL_TIME_OUT, TimeUnit.SECONDS)
                    cache(cache())

                    if (BuildConfig.DEBUG) {
                        addInterceptor(loggingInterceptor())
                    }

                }
                .build()

        fun moshi() = Moshi.Builder().build()

        fun moshiConverterFactory() = MoshiConverterFactory.create(moshi())

        fun retrofitBuilder() = Retrofit.Builder()
                .client(okHttpClient())
                .addConverterFactory(moshiConverterFactory())

        return retrofitBuilder().baseUrl(baseUrl).build().create(kClass)
    }

}
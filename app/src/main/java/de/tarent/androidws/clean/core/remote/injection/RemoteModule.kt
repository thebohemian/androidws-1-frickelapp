package de.tarent.androidws.clean.core.concurrency.injection

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import de.tarent.androidws.clean.BuildConfig
import de.tarent.androidws.clean.core.remote.ServiceCreator
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.rewedigital.katana.Module
import org.rewedigital.katana.android.modules.APPLICATION_CONTEXT
import org.rewedigital.katana.dsl.factory
import org.rewedigital.katana.dsl.get
import org.rewedigital.katana.dsl.singleton
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val CONNECT_TIME_OUT = 5L
private const val CALL_TIME_OUT = 5L
private const val READ_TIME_OUT = 10L
private const val WRITE_TIME_OUT = 10L
private const val CACHE_SIZE_BYTES = 1024 * 1024 * 10L

val RemoteModule = Module {

    factory { Cache(get<Context>(APPLICATION_CONTEXT).cacheDir, CACHE_SIZE_BYTES) }

    factory<HttpLoggingInterceptor.Logger> {
        object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("RestoHttp", message)
            }
        }
    }

    singleton { HttpLoggingInterceptor(get()).apply { level = HttpLoggingInterceptor.Level.BODY } }

    singleton {
        OkHttpClient.Builder()
                .apply {
                    connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                    readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                    writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                    callTimeout(CALL_TIME_OUT, TimeUnit.SECONDS)
                    cache(get())

                    if (BuildConfig.DEBUG) {
                        addInterceptor(get<HttpLoggingInterceptor>())
                    }

                }
                .build()
    }

    singleton<Moshi> { Moshi.Builder().build() }

    singleton<MoshiConverterFactory> {
        MoshiConverterFactory.create(get())
    }

    singleton<Retrofit.Builder> {
        Retrofit.Builder()
                .client(get())
                .addConverterFactory(get<MoshiConverterFactory>())
    }

    singleton { ServiceCreator(get()) }
}
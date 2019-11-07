package de.tarent.androidws.clean.core.remote

import retrofit2.Retrofit

class ServiceCreator(private val retrofitBuilder: Retrofit.Builder) {

    operator fun <T> invoke(baseUrl: String, kClass: Class<out T>): T {

        return retrofitBuilder.baseUrl(baseUrl).build().create(kClass)
    }

}
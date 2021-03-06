package de.tarent.androidws.clean.repository.restaurant.remote

import de.tarent.androidws.clean.core.remote.ServiceCreator
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant
import retrofit2.Response
import retrofit2.http.GET

interface RestaurantsRemote {

    @GET(value = "restaurants")
    suspend fun getRestaurants(): Response<List<Restaurant>>

    companion object {
        internal fun create(
                serviceCreator: ServiceCreator,
                baseUrl: String) = serviceCreator(
                baseUrl = baseUrl,
                kClass = RestaurantsRemote::class.java)

    }
}
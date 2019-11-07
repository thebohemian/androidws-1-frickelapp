package de.tarent.androidws.clean.repository.restaurant

import retrofit2.Response
import retrofit2.http.GET

interface RestaurantsRemote {

    @GET(value = "restaurants")
    suspend fun getRestaurants(): Response<List<Restaurant>>

}
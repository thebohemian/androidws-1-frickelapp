package de.tarent.androidws.clean.repository.restaurant.repository

import de.tarent.androidws.clean.repository.restaurant.remote.RestaurantsRemote

interface RestaurantRepository {

    // TODO: Define and implement
    /*fun getRestaurants()*/

}

internal class RestaurantRepositoryImpl(
        private val remote: RestaurantsRemote
) : RestaurantRepository {


}
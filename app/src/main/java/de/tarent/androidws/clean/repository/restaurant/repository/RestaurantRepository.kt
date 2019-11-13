package de.tarent.androidws.clean.repository.restaurant.repository

import de.tarent.androidws.clean.repository.common.extension.flagDataError
import de.tarent.androidws.clean.repository.common.extension.flagIoError
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant
import de.tarent.androidws.clean.repository.restaurant.remote.RestaurantsRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.io.IOException

interface RestaurantRepository {

    fun getRestaurants(): Flow<List<Restaurant>>

}

internal class RestaurantRepositoryImpl(
        private val remote: RestaurantsRemote
) : RestaurantRepository {

    override fun getRestaurants(): Flow<List<Restaurant>> = flow {
        try {
            with(remote.getRestaurants()) {
                when {
                    isSuccessful -> body()?.let { emit(it) }
                            ?: flagDataError("empty body")
                    else -> flagDataError("unable to parse data")
                }
            }
        } catch (ioe: IOException) {
            flagIoError("exception occured", ioe)
        }
    }
}
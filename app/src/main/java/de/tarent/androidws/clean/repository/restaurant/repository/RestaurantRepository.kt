package de.tarent.androidws.clean.repository.restaurant.repository

import android.content.Context
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.core.ServiceCreator
import de.tarent.androidws.clean.repository.common.extension.flagDataError
import de.tarent.androidws.clean.repository.common.extension.flagGeneralError
import de.tarent.androidws.clean.repository.common.extension.flagIoError
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant
import de.tarent.androidws.clean.repository.restaurant.remote.RestaurantsRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

interface RestaurantRepository {

    suspend fun getRestaurants(): Flow<List<Restaurant>>

    companion object {
        internal fun create(context: Context, serviceCreator: ServiceCreator): RestaurantRepository =
                RestaurantRepositoryImpl(
                        serviceCreator(
                                context = context,
                                baseUrl = context.getString(R.string.service_url),
                                kClass = RestaurantsRemote::class.java)
                )
    }

}

internal class RestaurantRepositoryImpl(
        private val remote: RestaurantsRemote
) : RestaurantRepository {

    override suspend fun getRestaurants(): Flow<List<Restaurant>> = flow {
        // TODO: Interact with the RestaurantsRemote and send the data
        // via the flow
        // detect error situations and flag them in the flow using respective
        // extension function
        flagGeneralError("Not yet implemented")
    }

}
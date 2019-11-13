package de.tarent.androidws.clean.feature.restaurant.usecase

import de.tarent.androidws.clean.repository.restaurant.repository.RestaurantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@UseExperimental(ExperimentalCoroutinesApi::class)
internal class GetRestaurantUseCase(
        private val context: CoroutineContext,
        private val restaurantRepository: RestaurantRepository) {

    operator fun invoke() = restaurantRepository
            .getRestaurants()
            .flowOn(context)

}
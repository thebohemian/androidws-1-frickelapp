package de.tarent.androidws.clean.feature.restaurant.usecase

import de.tarent.androidws.clean.repository.restaurant.repository.RestaurantRepository
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class GetRestaurantUseCase(
        private val context: CoroutineContext,
        private val restaurantRepository: RestaurantRepository
) {

    suspend operator fun invoke() = withContext(context) {
        restaurantRepository.getRestaurants()
    }

}
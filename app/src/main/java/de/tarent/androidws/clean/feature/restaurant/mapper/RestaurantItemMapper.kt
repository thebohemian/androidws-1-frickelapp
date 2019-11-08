package de.tarent.androidws.clean.feature.restaurant.mapper

import de.tarent.androidws.clean.feature.restaurant.model.RestaurantItem
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant

internal class RestaurantItemMapper {
    operator fun invoke(restaurant: Restaurant): RestaurantItem =
            RestaurantItem(
                    restaurant = restaurant,
                    checked = false)
}
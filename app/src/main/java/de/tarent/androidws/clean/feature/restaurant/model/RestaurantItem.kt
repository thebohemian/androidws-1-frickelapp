package de.tarent.androidws.clean.feature.restaurant.model

import de.tarent.androidws.clean.repository.restaurant.model.Restaurant

internal data class RestaurantItem(
        val restaurant: Restaurant,
        val checked: Boolean)
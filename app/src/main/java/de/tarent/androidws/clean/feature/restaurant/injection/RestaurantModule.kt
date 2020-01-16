package de.tarent.androidws.clean.feature.restaurant.injection

import de.tarent.androidws.clean.feature.restaurant.mapper.RestaurantItemMapper
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import de.tarent.androidws.clean.feature.restaurant.view.binder.RestaurantListViewStateBinder
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel
import org.rewedigital.katana.Module
import org.rewedigital.katana.androidx.viewmodel.viewModel
import org.rewedigital.katana.dsl.factory

val RestaurantModule = Module {

    factory { RestaurantListViewStateBinder() }

    viewModel<RestaurantListViewModel> { throw UnsupportedOperationException("Implement me!") /* TODO: Create instance here */ }

}
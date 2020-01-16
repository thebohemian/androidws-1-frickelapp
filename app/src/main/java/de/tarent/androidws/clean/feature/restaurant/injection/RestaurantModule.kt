package de.tarent.androidws.clean.feature.restaurant.injection

import de.tarent.androidws.clean.core.concurrency.injection.IO_DISPATCHER
import de.tarent.androidws.clean.feature.restaurant.mapper.RestaurantItemMapper
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import de.tarent.androidws.clean.feature.restaurant.view.binder.RestaurantListViewStateBinder
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModelImpl
import org.rewedigital.katana.Module
import org.rewedigital.katana.androidx.viewmodel.viewModel
import org.rewedigital.katana.dsl.factory
import org.rewedigital.katana.dsl.get

val RestaurantModule = Module {

    factory { RestaurantItemMapper() }

    factory { GetRestaurantUseCase(get(IO_DISPATCHER), get()) }

    factory { RestaurantListViewStateBinder() }

    viewModel<RestaurantListViewModel> { RestaurantListViewModelImpl(get(), get()) }

}
package de.tarent.androidws.clean.feature.restaurant.injection

import de.tarent.androidws.clean.core.concurrency.injection.IO_CONTEXT
import de.tarent.androidws.clean.feature.qrscanner.viewmodel.FinderSharedViewModel
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

    factory<RestaurantItemMapper> { RestaurantItemMapper() }

    factory<GetRestaurantUseCase> { GetRestaurantUseCase(get(IO_CONTEXT), get()) }

    factory<RestaurantListViewStateBinder> { RestaurantListViewStateBinder() }

    viewModel<FinderSharedViewModel> { FinderSharedViewModel() }

    viewModel<RestaurantListViewModel> { RestaurantListViewModelImpl(get(), get()) }

}
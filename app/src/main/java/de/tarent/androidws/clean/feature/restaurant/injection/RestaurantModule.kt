package de.tarent.androidws.clean.feature.restaurant.injection

import de.tarent.androidws.clean.core.concurrency.injection.IO_CONTEXT
import de.tarent.androidws.clean.feature.qrscanner.viewmodel.FinderSharedViewModel
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import org.rewedigital.katana.Module
import org.rewedigital.katana.androidx.viewmodel.viewModel
import org.rewedigital.katana.dsl.factory
import org.rewedigital.katana.dsl.get

val RestaurantModule = Module {

    factory<GetRestaurantUseCase> { GetRestaurantUseCase(get(IO_CONTEXT), get()) }

    viewModel<FinderSharedViewModel> { FinderSharedViewModel() }
}
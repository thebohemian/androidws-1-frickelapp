package de.tarent.androidws.clean.feature.restaurant.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.tarent.androidws.clean.feature.restaurant.mapper.RestaurantItemMapper
import de.tarent.androidws.clean.feature.restaurant.model.RestaurantItem
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import de.tarent.androidws.clean.repository.common.RepositoryException
import de.tarent.androidws.clean.repository.common.extension.onFail
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal abstract class RestaurantListViewModel : ViewModel() {

    sealed class State {
        object Initial : State()
        data class Loading(val isRetryOrInitial: Boolean) : State()
        data class Content(val list: List<RestaurantItem>) : State()
        object NetworkError : State()
        object GeneralError : State()
    }

    abstract val state: LiveData<State>

    abstract fun load(useForce: Boolean = false)

}

@UseExperimental(ExperimentalCoroutinesApi::class)
internal class RestaurantListViewModelImpl(
        // TODO: Arguments
) : RestaurantListViewModel() {

    // TODO: Implementation

}
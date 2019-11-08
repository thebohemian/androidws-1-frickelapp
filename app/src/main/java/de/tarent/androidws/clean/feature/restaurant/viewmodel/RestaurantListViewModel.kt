package de.tarent.androidws.clean.feature.restaurant.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import de.tarent.androidws.clean.repository.common.RepositoryException
import de.tarent.androidws.clean.repository.common.extension.onFail
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class RestaurantListViewModel : ViewModel() {

    sealed class State {
        object Initial : State()
        data class Loading(val isRetryOrInitial: Boolean) : State()
        data class Content(val list: List<Restaurant>) : State()
        object Error : State()
    }

    abstract val state: LiveData<State>

    abstract fun load(useForce: Boolean = false)

    abstract fun lookup(name: String, onSuccess: () -> Unit)
}

@ExperimentalCoroutinesApi
internal class RestaurantListViewModelImpl(
        private val getRestaurantUseCase: GetRestaurantUseCase
) : RestaurantListViewModel() {

    private val mutableState = MutableLiveData<State>()

    override val state = mutableState

    override fun load(useForce: Boolean) {
        when (state.value) {
            is State.Loading -> Unit
            is State.Content -> if (useForce) doLoad(false)
            else -> doLoad(true) // Initial, Error
        }
    }

    private fun doLoad(isRetryOrInitial: Boolean) {
        mutableState.value = State.Loading(isRetryOrInitial)

        viewModelScope.launch {
            getRestaurantUseCase()
                    .onFail { onFail(it) }
                    .onEach { onData(it) }
                    .collect()
        }
    }

    private fun onData(list: List<Restaurant>) {
        mutableState.value = State.Content(
                list = list)
    }

    private fun onFail(cause: RepositoryException) {
        Log.d(TAG, "retrieving restaurants failed: ${cause.message}")
        mutableState.value = State.Error
    }

    override fun lookup(name: String, onSuccess: () -> Unit) {
    }

    companion object {
        private var TAG = "RestaurantListVM"
    }
}
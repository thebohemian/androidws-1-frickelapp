package de.tarent.androidws.clean.feature.restaurant.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tarent.androidws.clean.core.viewmodel.EventHolder
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import de.tarent.androidws.clean.repository.common.RepositoryException
import de.tarent.androidws.clean.repository.common.extension.onFail
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

abstract class RestaurantListViewModel : ViewModel() {

    sealed class Event {
        object None : Event()
        data class LookedUp(val index: Int, val name: String) : Event()
        data class LookUpFailed(val name: String) : Event()
    }

    sealed class State {
        object Initial : State()
        data class Loading(val isRetryOrInitial: Boolean) : State()
        data class Content(val list: List<Restaurant>) : State()
        object Error : State()
    }

    abstract val state: LiveData<State>

    abstract val event: LiveData<EventHolder<Event>>

    abstract fun load(useForce: Boolean = false)

    abstract fun tryLookup(name: String)
}

@ExperimentalCoroutinesApi
internal class RestaurantListViewModelImpl(
        private val getRestaurantUseCase: GetRestaurantUseCase
) : RestaurantListViewModel() {

    private val mutableState = MutableLiveData<State>(State.Initial)

    private val mutableEvent = MutableLiveData(EventHolder<Event>(Event.None))

    override val state = mutableState

    override val event = mutableEvent

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

    private fun doTryLookUp(list: List<Restaurant>, name: String) {
        val index = list.indexOfFirst {
            it.name.toLowerCase(Locale.getDefault()) == name.toLowerCase(Locale.getDefault())
        }

        mutableEvent.value = EventHolder(if (index != NOT_FOUND) Event.LookedUp(
                index = index,
                name = name
        ) else Event.LookUpFailed(
                name = name
        ))

    }

    private fun onFail(cause: RepositoryException) {
        Log.d(TAG, "retrieving restaurants failed: ${cause.message}")
        mutableState.value = State.Error
    }

    override fun tryLookup(name: String) {
        when (val stateValue = state.value) {
            is State.Content -> doTryLookUp(stateValue.list, name)
            /* TODO: In the Loading and Initial state it should also
            * be possible to do a lookup. As we dont have data yet,
            * it needs to be done later somehow.
            is State.Loading ->
            State.Initial ->
             */
        }
    }

    companion object {
        private var TAG = "RestaurantListVM"

        private const val NOT_FOUND = -1
    }
}
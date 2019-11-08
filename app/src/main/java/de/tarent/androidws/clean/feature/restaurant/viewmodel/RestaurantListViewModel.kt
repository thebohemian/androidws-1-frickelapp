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
import kotlinx.coroutines.flow.*
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
        object NetworkError : State()
        object GeneralError : State()
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

    private var nameToLookUp: String? = null

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
                    /* Creates a random grave error situation
                    .flatMapMerge {
                        flow<List<Restaurant>> {
                            if (Math.random() > 0.5)
                                throw Error("Evil!")
                        }
                    }
                    */
                    .catch { onGraveFail(it) }
                    .collect()
        }
    }

    private fun onData(list: List<Restaurant>) {
        Log.d(TAG, "gotten data! first element: ${list[0]}")
        mutableState.value = State.Content(
                list = list)

        nameToLookUp?.let {
            nameToLookUp = null

            doTryLookUp(list, it)
        }
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
        mutableState.value = State.NetworkError
    }

    private fun onGraveFail(cause: Throwable) {
        Log.d(TAG, "caught unexpected exception: ${cause}")
        mutableState.value = State.GeneralError
    }

    override fun tryLookup(name: String) {
        when (val stateValue = state.value) {
            is State.Content -> doTryLookUp(stateValue.list, name)
            is State.Loading -> nameToLookUp = name
            State.Initial -> nameToLookUp = name
        }
    }

    companion object {
        private var TAG = "RestaurantListVM"

        private const val NOT_FOUND = -1
    }
}
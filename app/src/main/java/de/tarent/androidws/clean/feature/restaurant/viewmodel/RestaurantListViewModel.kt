package de.tarent.androidws.clean.feature.restaurant.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tarent.androidws.clean.core.viewmodel.EventHolder
import de.tarent.androidws.clean.feature.restaurant.mapper.RestaurantItemMapper
import de.tarent.androidws.clean.feature.restaurant.model.RestaurantItem
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import de.tarent.androidws.clean.repository.common.RepositoryException
import de.tarent.androidws.clean.repository.common.extension.onFail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.*

internal abstract class RestaurantListViewModel : ViewModel() {

    sealed class Event {
        object None : Event()
        data class LookedUp(val index: Int, val item: RestaurantItem) : Event()
        data class LookUpFailed(val name: String) : Event()
    }

    sealed class State {
        object Initial : State()
        data class Loading(val isRetryOrInitial: Boolean) : State()
        data class Content(val list: List<RestaurantItem>) : State()
        object NetworkError : State()
        object GeneralError : State()
    }

    abstract val state: LiveData<State>

    abstract val event: LiveData<EventHolder<Event>>

    abstract fun load(useForce: Boolean = false)

    abstract fun tryLookup(name: String)
    abstract fun toggleChecked(item: RestaurantItem)
}

@UseExperimental(ExperimentalCoroutinesApi::class)
internal class RestaurantListViewModelImpl(
        private val getRestaurantUseCase: GetRestaurantUseCase,
        private val restaurantItemMapper: RestaurantItemMapper
) : RestaurantListViewModel() {

    private val mutableState = MutableLiveData<State>().apply { setValue(State.Initial) }

    private val mutableEvent = MutableLiveData<EventHolder<Event>>().apply { setValue(EventHolder<Event>(Event.None)) }

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

        getRestaurantUseCase()
                .map { list -> list.map { resto -> restaurantItemMapper(resto) } }
                .onFail { onFail(it) }
                .onEach { onData(it) }
                /* Creates a random grave error situation
                .flatMapMerge {
                    flow<List<RestaurantItem>> {
                        if (Math.random() > 0.5)
                            throw Error("Evil!")
                    }
                }
                */
                .catch { onGraveFail(it) }
                .launchIn(viewModelScope)
    }

    private fun onData(list: List<RestaurantItem>) {
        //Log.d(TAG, "gotten data! first element: ${list[0]}")
        mutableState.value = State.Content(
                list = list)

        nameToLookUp?.let {
            nameToLookUp = null

            doTryLookUp(list, it)
        }
    }

    private fun doTryLookUp(list: List<RestaurantItem>, name: String) {
        val index = list.indexOfFirst {
            it.restaurant.name.toLowerCase(Locale.getDefault()) == name.toLowerCase(Locale.getDefault())
        }

        mutableEvent.value = EventHolder(if (index != NOT_FOUND) Event.LookedUp(
                index = index,
                item = list[index]
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

    override fun toggleChecked(item: RestaurantItem) {
        when (val stateValue = state.value) {
            is State.Content -> doToggleChecked(item, stateValue)
        }
    }

    private fun doToggleChecked(item: RestaurantItem, content: State.Content) {
        val index = content.list.indexOf(item)
        if (index >= NOT_FOUND) {
            // Replaces the element in the list at "index" with
            // an item that has it's checked flag reversed
            val newList = content.list.mapIndexed { idx, it ->
                if (index == idx)
                    it.copy(
                            checked = !item.checked
                    )
                else it
            }

            // Writes the state anew, causing an update of the RecyclerView's list
            mutableState.value =
                    content.copy(
                            list = newList
                    )
        }
    }

    companion object {
        private var TAG = "RestaurantListVM"

        private const val NOT_FOUND = -1
    }
}
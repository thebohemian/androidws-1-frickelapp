package de.tarent.androidws.clean.feature.qrscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tarent.androidws.clean.core.viewmodel.EventHolder

/**
 * Shared ViewModel which takes the return value of the [FinderFragment]
 * and makes it available to whoever uses it (still limited to the
 * same nav graph)
 */
class FinderSharedViewModel : ViewModel() {

    private val mutableNameEvent = MutableLiveData<EventHolder<String>>()

    val nameEvent: LiveData<EventHolder<String>> = mutableNameEvent

    fun put(name: String) {
        mutableNameEvent.value = EventHolder(name)
    }

}
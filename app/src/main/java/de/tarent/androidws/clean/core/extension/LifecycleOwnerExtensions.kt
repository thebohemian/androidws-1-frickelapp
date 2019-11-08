package de.tarent.androidws.clean.core.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import de.tarent.androidws.clean.core.viewmodel.EventHolder

fun <T : Any> LifecycleOwner.observe(liveData: LiveData<T>, body: (T) -> Unit) {
    liveData.observe(this, Observer(body))
}

/*
 * Observe [LiveData] that is encapsulated in a [Event] instance.
 *
 * Same as [observe] except that the given body will not be invoked when the [Event] instance's
 * data is not available anymore. In other words, the event can only be handled once.
 */
fun <T : Any> LifecycleOwner.observeEvent(liveData: LiveData<EventHolder<T>>, body: (T) -> Unit) {
    liveData.observe(this, Observer { it?.getOnce()?.let(body) })
}
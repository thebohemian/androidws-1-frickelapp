package de.tarent.androidws.clean.feature.qrscanner.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Shared ViewModel which takes the return value of the [FinderFragment]
 * and makes it available to whoever uses it (still limited to the
 * same nav graph)
 */
class FinderSharedViewModel : ViewModel() {

    private var name: String? = null

    fun put(name: String) {
        this.name = name
    }

    fun requestPeek(block: (String) -> Unit) {
        name?.let {
            name = null
            block(it)
        }
    }

}
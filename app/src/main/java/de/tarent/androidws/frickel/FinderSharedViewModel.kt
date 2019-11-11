package de.tarent.androidws.frickel

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

    // TODO: Add a function which under the condition that currently a value is stored
    // delivers the result and clears the stored value

}
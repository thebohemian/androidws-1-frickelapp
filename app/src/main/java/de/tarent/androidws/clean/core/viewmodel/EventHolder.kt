package de.tarent.androidws.clean.core.viewmodel

class EventHolder<out T>(private val content: T) {

    var hasBeenGottenOnce = false
        private set // Allow external read but not write

    /**
     * Returns the content exactly one time.
     *
     * Any second invocation of this method will return null.
     */
    fun getOnce(): T? {
        return if (hasBeenGottenOnce) {
            null
        } else {
            hasBeenGottenOnce = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
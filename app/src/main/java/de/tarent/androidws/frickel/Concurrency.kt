package de.tarent.androidws.frickel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object Concurrency {

    private fun ioDispatcher() = Dispatchers.IO

    fun ioContext() = ioDispatcher() + Job()

}
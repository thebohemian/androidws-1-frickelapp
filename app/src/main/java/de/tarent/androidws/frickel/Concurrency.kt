package de.tarent.androidws.frickel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object Concurrency {

    private fun ioDispatcher() = Dispatchers.IO

    private fun ioContext() = ioDispatcher() + Job()

    fun ioScope() = CoroutineScope(ioContext())

}
package de.tarent.androidws.clean

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object Concurrency {

    fun ioDispatcher() = Dispatchers.IO

    fun ioContext() = ioDispatcher() + Job()

}
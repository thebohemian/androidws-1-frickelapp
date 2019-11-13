package de.tarent.androidws.clean.core.concurrency.injection

import de.tarent.androidws.clean.core.concurrency.Concurrency
import org.rewedigital.katana.Module
import org.rewedigital.katana.dsl.singleton
import kotlin.coroutines.CoroutineContext

const val IO_CONTEXT = "ioContext"

const val IO_DISPATCHER = "ioDispatcher"

val CoreModule = Module {

    singleton<CoroutineContext>(IO_DISPATCHER) { Concurrency.ioDispatcher() }

    singleton<CoroutineContext>(IO_CONTEXT) { Concurrency.ioContext() }

}
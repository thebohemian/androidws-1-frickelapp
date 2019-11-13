package de.tarent.androidws.clean.repository.common.extension

import de.tarent.androidws.clean.repository.common.RepositoryException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch

@ExperimentalCoroutinesApi
fun <T> Flow<T>.onFail(action: suspend FlowCollector<T>.(cause: RepositoryException) -> Unit): Flow<T> =
        catch { cause ->
            if (cause is RepositoryException) {
                action(cause)
            } else throw cause
        }

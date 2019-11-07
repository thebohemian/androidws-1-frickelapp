package de.tarent.androidws.clean.repository.common.extension

import de.tarent.androidws.clean.repository.common.RepositoryException
import kotlinx.coroutines.flow.FlowCollector

internal fun <T> FlowCollector<T>.flagGeneralError(msg: String = "", throwable: Throwable? = null): Nothing {
    throw RepositoryException(RepositoryException.Status.GENERAL_ERROR, msg, throwable)
}

internal fun <T> FlowCollector<T>.flagDataError(msg: String = "", throwable: Throwable? = null): Nothing {
    throw RepositoryException(RepositoryException.Status.DATA_ERROR, msg, throwable)
}

internal fun <T> FlowCollector<T>.flagIoError(msg: String = "", throwable: Throwable? = null): Nothing {
    throw RepositoryException(RepositoryException.Status.IO_ERROR, msg, throwable)
}

internal fun <T> FlowCollector<T>.flagNetworkError(msg: String = "", throwable: Throwable? = null): Nothing {
    throw RepositoryException(RepositoryException.Status.NETWORK_ERROR, msg, throwable)
}
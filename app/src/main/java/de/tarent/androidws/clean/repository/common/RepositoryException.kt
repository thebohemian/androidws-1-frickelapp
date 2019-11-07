package de.tarent.androidws.clean.repository.common

class RepositoryException internal constructor(
        val status: Status,
        message: String = "",
        cause: Throwable? = null) : Exception(message, cause) {

    enum class Status {
        NETWORK_ERROR,
        IO_ERROR,
        DATA_ERROR,
        GENERAL_ERROR
    }

}
package de.tarent.androidws.clean.feature.qrscanner.extensions

import de.tarent.androidws.clean.feature.qrscanner.view.FinderFragment

internal fun FinderFragment._debugReturnEarly() {
    view?.setOnClickListener {
        handleNameDetected("asia kitchen")
    }

}
package de.tarent.androidws.clean.feature.qrscanner.injection

import de.tarent.androidws.clean.feature.qrscanner.view.FirebaseMLAnalyzer
import de.tarent.androidws.clean.feature.qrscanner.view.QRCodeDetector
import org.rewedigital.katana.Module
import org.rewedigital.katana.dsl.factory
import org.rewedigital.katana.dsl.get
import java.util.concurrent.Executor
import java.util.concurrent.Executors

val FinderModule = Module {

    factory<QRCodeDetector> { QRCodeDetector(get(), get()) }

    factory<FirebaseMLAnalyzer> { FirebaseMLAnalyzer() }

    factory<Executor> { Executors.newSingleThreadExecutor() }

}
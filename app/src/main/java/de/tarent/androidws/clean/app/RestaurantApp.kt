package de.tarent.androidws.clean.app

import android.app.Application
import de.tarent.androidws.clean.core.concurrency.injection.CoreModule
import de.tarent.androidws.clean.core.concurrency.injection.RemoteModule
import org.rewedigital.katana.Component
import org.rewedigital.katana.Katana
import org.rewedigital.katana.android.environment.AndroidEnvironmentContext
import org.rewedigital.katana.android.modules.ApplicationModule

class RestaurantApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Katana.environmentContext = AndroidEnvironmentContext()

        component = Component(
                modules = listOf(ApplicationModule(this), CoreModule, RemoteModule)
        )
    }

    companion object {
        private lateinit var component: Component

        fun applicationComponent() = component
    }
}
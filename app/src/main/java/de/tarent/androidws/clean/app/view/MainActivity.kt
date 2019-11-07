package de.tarent.androidws.clean.app.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.app.RestaurantApp
import de.tarent.androidws.clean.app.injection.AppModule
import de.tarent.androidws.clean.repository.restaurant.injection.RestaurantRepositoryModule
import org.rewedigital.katana.Component
import org.rewedigital.katana.KatanaTrait

class MainActivity : AppCompatActivity(), KatanaTrait {

    override val component = Component(
            modules = listOf(AppModule),
            dependsOn = listOf(RestaurantApp.applicationComponent())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.component_activity_main)
    }

}

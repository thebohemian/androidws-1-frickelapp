package de.tarent.androidws.frickel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val adapter = RestaurantListAdapter()

    private lateinit var restaurantsRemote: RestaurantsRemote

    private val ioScope = Concurrency.ioScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        restaurantsRemote = ServiceCreator(
                context = this,
                baseUrl = getString(R.string.service_url),
                kClass = RestaurantsRemote::class.java)

        restaurantsList.adapter = adapter

        ioScope.launch {
            // TODO: Get the restaurant data using RestaurantsRemote
            // and give it to the RestaurantAdapter somehow

            // Beware: Retrofit runs on IO Thread but the adapter
            // needs to be called from Main (or UI) thread.
            
            // LiveData can help
        }
    }

}

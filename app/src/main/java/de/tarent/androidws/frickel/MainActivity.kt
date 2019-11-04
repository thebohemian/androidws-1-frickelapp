package de.tarent.androidws.frickel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val restaurantLiveData = MutableLiveData<List<Restaurant>>()

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

        restaurantLiveData.observe(
                this,
                Observer<List<Restaurant>> {
                    adapter.submitList(it)
                })

        ioScope.launch {
            with(restaurantsRemote.getRestaurants()) {
                body()?.let(restaurantLiveData::postValue)
            }
        }
    }

}

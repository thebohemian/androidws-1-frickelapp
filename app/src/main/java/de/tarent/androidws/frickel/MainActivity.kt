package de.tarent.androidws.frickel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOError
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val restaurantLiveData = MutableLiveData<List<Restaurant>>()

    private val adapter = RestaurantListAdapter()

    private lateinit var restaurantsRemote: RestaurantsRemote

    private val ioContext by lazy { Concurrency.ioContext() }

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
                    handleDataAvailable(it)
                })

        adapter.onRestaurantClickListener = { restaurant ->
            Toast.makeText(this, "Clicked: ${restaurant.name}", Toast.LENGTH_SHORT).show()
        }

        loadButton.setOnClickListener { loadButtonClicked() }

        fab.setOnClickListener {
            startActivity(Intent(this, FinderActivity::class.java))
        }
    }

    private fun loadButtonClicked() {
        handleLoading()

        lifecycleScope.launch {
            getRestaurants(restaurantLiveData::postValue)
        }
    }

    suspend fun getRestaurants(block: (List<Restaurant>) -> Unit) = withContext(ioContext) {
        /* TODO: Maybe bundle all what is needed in a method, so
        * it can be called from different places in this
        * method body.
        fun errorOut() {
        }
         */

        try {
            with(restaurantsRemote.getRestaurants()) {
                when {
                    isSuccessful -> body().let { block(it) } // TODO: some handling necessary
                    else -> Unit // TODO: should do something about it
                }

            }
        } catch (ioe: IOException) {
            // TODO: should do something about it
        }
    }

    private fun handleLoading() {
        progress.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        restaurantsList.visibility = View.GONE
    }

    private fun handleDataAvailable(restaurants: List<Restaurant>) {
        progress.visibility = View.GONE
        restaurantsList.visibility = View.VISIBLE
        adapter.submitList(restaurants)
    }

    private fun handleError() {
        // TODO: The dedicated error views should be visible,
        // the normal view and loading indicators should not
    }

}

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

        restaurantList.adapter = adapter

        restaurantLiveData.observe(
                this,
                Observer<List<Restaurant>> {
                    handleDataAvailable(it)
                })

        adapter.onRestaurantClickListener = { restaurant ->
            Toast.makeText(this, "Clicked: ${restaurant.name}", Toast.LENGTH_SHORT).show()
        }

        restaurantListSwipeRefresh.setOnRefreshListener {
            // TODO: Here we load because of an explicit refresh
            // request
            loadData()
        }

        fab.setOnClickListener {
            startActivity(Intent(this, FinderActivity::class.java))
        }

        // TODO: Here we load initially without user interaction
        loadData()
    }

    private fun loadData() {
        // TODO: Somehow it must be possible to distinguish between
        // initial loading, retried loading after an error and
        // swipe2refresh requests
        handleLoading()

        lifecycleScope.launch {
            getRestaurants(restaurantLiveData::postValue)
        }
    }

    suspend fun getRestaurants(block: (List<Restaurant>) -> Unit) = withContext(ioContext) {
        // Safely handle the error on UI thread
        fun errorOut() {
            runOnUiThread {
                handleError()
            }
        }

        try {
            with(restaurantsRemote.getRestaurants()) {
                when {
                    isSuccessful -> body()?.let { block(it) } ?: errorOut()
                    else -> errorOut()
                }

            }
        } catch (ioe: IOException) {
            errorOut()
        }
    }

    private fun handleLoading() {
        // TODO:
        // The big progress should only be visible
        // when we load initially or are retrying.

        // However when we are loading again
        // because of swipe2refresh, the recycler
        // should better not react on anything.

        errorLayout.visibility = View.GONE
        retryButton.setOnClickListener(null)
    }

    private fun handleDataAvailable(restaurants: List<Restaurant>) {
        progress.visibility = View.GONE

        errorLayout.visibility = View.GONE
        retryButton.setOnClickListener(null)

        restaurantListSwipeRefresh.visibility = View.VISIBLE
        restaurantListSwipeRefresh.isRefreshing = false
        restaurantList.isEnabled = true
        adapter.submitList(restaurants)
    }

    private fun handleError() {
        progress.visibility = View.GONE

        errorLayout.visibility = View.VISIBLE
        retryButton.setOnClickListener {
            // TODO: Here we load because it is a retry!
            loadData()
        }

        restaurantListSwipeRefresh.visibility = View.GONE
        restaurantListSwipeRefresh.isRefreshing = false
        adapter.submitList(emptyList())
    }

}

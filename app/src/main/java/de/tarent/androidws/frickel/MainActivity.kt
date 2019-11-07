package de.tarent.androidws.frickel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.component_restaurant_item.view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    /**
     * Keeps the data and it can be listened on for data changes.
     */
    private val restaurantLiveData = MutableLiveData<List<Restaurant>>()

    /**
     *  Data handling specifically for RecyclerView (also possible for ViewPager2)
     */
    private val adapter = RestaurantListAdapter()

    /**
     * Temporary storage for a restaurant that needs to be looked up.
     *
     * When not null, then a look up is necessary.
     */
    private var lookingForRestaurant: String? = null

    /**
     * Retrofit-implemented interface that does calls to the
     * backend.
     */
    private lateinit var restaurantsRemote: RestaurantsRemote

    /**
     * Needed for putting backend calls on a different thread.
     */
    private val ioContext by lazy { Concurrency.ioContext() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Creates interface for calling backend
        restaurantsRemote = ServiceCreator(
                context = this,
                baseUrl = getString(R.string.service_url),
                kClass = RestaurantsRemote::class.java)

        // Makes RecyclerView use the restaurant adapter
        restaurantList.adapter = adapter

        // Makes handleDataAvailable being called whenever new values
        // are written into restaurantLiveData.
        // By definition runs on the UI-thread
        restaurantLiveData.observe(
                this,
                Observer<List<Restaurant>> {
                    handleDataAvailable(it)
                })

        // Wires a listener to the adapter which tells when a click
        // on a restaurant item happened.
        // For the moment shows a toast, might open a detail view later
        adapter.onRestaurantClickListener = { restaurant ->
            Toast.makeText(this, "Clicked: ${restaurant.name}", Toast.LENGTH_SHORT).show()
        }

        // Handler for when "swipe refresh" gesture was done.
        restaurantListSwipeRefresh.setOnRefreshListener {
            loadData(false)
        }

        // Handler for floating action button
        fab.setOnClickListener {
            startActivity(Intent(this, FinderActivity::class.java))
        }

        // Automatic checking
        intent?.let { checkIntent(it) }

        // Final step:
        // Automatic data load upon opening of the activity.
        loadData()
    }

    private fun loadData(isInitialOrRetry: Boolean = true) {
        // Sets UI element into loading state
        handleLoading(isInitialOrRetry)

        // Retrieves data (scope is bound to lifecycle of the activity)
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
                    isSuccessful -> body()?.let { block(it) } ?: errorOut() // -> data error
                    else -> errorOut()      // -> HTTP or data error
                }

            }
        } catch (ioe: IOException) {
            errorOut()  // -> IO-related error (dns, network, timeout, ...)
        }
    }

    private fun handleLoading(isInitialOrRetry: Boolean) {
        if (isInitialOrRetry) {
            // Plays progress animation
            progress.visibility = View.VISIBLE

            // Makes restaurant list and swiperfresh stuff invisible
            restaurantListSwipeRefresh.visibility = View.GONE
        } else {
            // Just disable interaction with restaurant list
            restaurantList.isEnabled = false
        }

        errorLayout.visibility = View.GONE
        retryButton.setOnClickListener(null)
    }

    private fun handleDataAvailable(restaurants: List<Restaurant>) {
        // Hides progress animation
        progress.visibility = View.GONE

        // Hides error views
        errorLayout.visibility = View.GONE
        retryButton.setOnClickListener(null)

        // Makes restaurant list visible and stops swipe refresh animation
        restaurantListSwipeRefresh.visibility = View.VISIBLE
        restaurantListSwipeRefresh.isRefreshing = false
        restaurantList.isEnabled = true
        adapter.submitList(restaurants)

        // UI is not entirely set up in this "frame". However
        // lookupForRestaurant() might want to mess with that views.
        // So let one render cycle pass and do the lookup in the
        // next one, so the views are available.
        //
        // Remove this ".post" and see what happens.
        restaurantList.post {
            // If a restaurant name to look up was stored,
            // do the lookup now
            lookingForRestaurant?.let {
                lookForRestaurantName(restaurants, it)
                lookingForRestaurant = null
            }

        }
    }

    private fun handleError() {
        // Sets up error view
        progress.visibility = View.GONE

        errorLayout.visibility = View.VISIBLE
        retryButton.setOnClickListener { loadData() }

        restaurantListSwipeRefresh.visibility = View.GONE
        restaurantListSwipeRefresh.isRefreshing = false
        adapter.submitList(emptyList())
    }

    private fun checkIntent(intent: Intent) {
        // Looks into intent and find out whether it has special data for us
        when (intent.action) {
            INTENT_ACTION_SCANNED_NAME -> {
                intent.getStringExtra(INTENT_EXTRA_NAME_KEY)
                        ?.let { lookingForRestaurant = it }
            }
            else -> {
                Log.d(TAG, "Was not a special intent.")
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let { checkIntent(it) }
    }

    private fun lookForRestaurantName(restos: List<Restaurant>, restaurantName: String) {
        // Finds index in our restaurant list (case being ignored)
        val index = restos.indexOfFirst {
            it.name.toLowerCase(Locale.getDefault()) == restaurantName.toLowerCase(Locale.getDefault())
        }

        if (index != NOT_FOUND) {
            // Finds the view in the recyclerview that represents our Restaurant object and manipulates
            // it
            // Assumption: Indices in restaurant list and indices in recyclerview adapter match (not
            // always the case)
            // This class has knowledge of the internals of the recyclerview items
            restaurantList.layoutManager
                    ?.findViewByPosition(index)
                    ?.restaurantCard
                    ?.performClick()
        } else {
            Snackbar.make(rootLayout, getString(R.string.restaurant_not_found, restaurantName), Snackbar.LENGTH_SHORT)
                    .show()
        }
    }

    companion object {
        private const val TAG = "MainAct"

        private const val NOT_FOUND = -1

        val INTENT_ACTION_SCANNED_NAME = "scanned_name"

        val INTENT_EXTRA_NAME_KEY = "name"
    }
}

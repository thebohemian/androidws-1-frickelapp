package de.tarent.androidws.clean.feature.restaurant.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.core.ServiceCreator
import de.tarent.androidws.clean.core.concurrency.Concurrency
import de.tarent.androidws.clean.feature.qrscanner.viewmodel.FinderSharedViewModel
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import de.tarent.androidws.clean.repository.common.extension.onFail
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant
import de.tarent.androidws.clean.repository.restaurant.repository.RestaurantRepository
import kotlinx.android.synthetic.main.component_fragment_restaurantlist.*
import kotlinx.android.synthetic.main.component_restaurant_item.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalCoroutinesApi
class RestaurantListFragment : Fragment() {

    private val finderSharedViewModel: FinderSharedViewModel by navGraphViewModels(R.id.nav_graph)

    /**
     * Keeps the data and it can be listened on for data changes.
     */
    private val restaurantLiveData = MutableLiveData<List<Restaurant>>()

    /**
     *  Data handling specifically for RecyclerView (also possible for ViewPager2)
     */
    private val adapter = RestaurantListAdapter()

    private lateinit var getRestaurantUseCase: GetRestaurantUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.component_fragment_restaurantlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.let { nonNullContext ->
            getRestaurantUseCase = GetRestaurantUseCase(
                    context = Concurrency.ioContext(),
                    restaurantRepository = RestaurantRepository.create(nonNullContext, ServiceCreator)
            )

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
                Toast.makeText(nonNullContext, "Clicked: ${restaurant.name}", Toast.LENGTH_SHORT).show()
            }

            // Handler for when "swipe refresh" gesture was done.
            restaurantListSwipeRefresh.setOnRefreshListener {
                loadData(false)
            }

            // Handler for floating action button
            fab.setOnClickListener {
                findNavController().navigate(R.id.action_restaurantListFragment_to_finderFragment)
            }

            // Final step:
            // Automatic data load upon opening of the activity.
            loadData()
        }
    }

    private fun loadData(isInitialOrRetry: Boolean = true) {
        // Sets UI element into loading state
        handleLoading(isInitialOrRetry)

        // Retrieves data (scope is bound to lifecycle of the activity)
        lifecycleScope.launch {
            getRestaurantUseCase()
                    .onFail { cause ->
                        Log.d(TAG, "retrieving restaurants failed: ${cause.message}")
                        activity?.runOnUiThread { handleError() }
                    }
                    .onEach {
                        restaurantLiveData.postValue(it)
                    }
                    .collect()
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
            finderSharedViewModel.requestPeek {
                lookForRestaurantName(restaurants, it)
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
        private const val TAG = "RestaurantListFrag"

        private const val NOT_FOUND = -1

    }
}

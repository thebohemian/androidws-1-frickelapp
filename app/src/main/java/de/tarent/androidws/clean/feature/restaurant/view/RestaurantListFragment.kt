package de.tarent.androidws.clean.feature.restaurant.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.feature.qrscanner.viewmodel.FinderSharedViewModel
import de.tarent.androidws.clean.feature.restaurant.injection.RestaurantModule
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel.State
import de.tarent.androidws.clean.repository.restaurant.injection.RestaurantRepositoryModule
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant
import kotlinx.android.synthetic.main.component_fragment_restaurantlist.*
import kotlinx.android.synthetic.main.component_restaurant_item.view.*
import org.rewedigital.katana.KatanaTrait
import org.rewedigital.katana.android.fragment.KatanaFragmentDelegate
import org.rewedigital.katana.android.fragment.fragmentDelegate
import org.rewedigital.katana.androidx.viewmodel.activityViewModelNow
import org.rewedigital.katana.androidx.viewmodel.viewModelNow
import java.util.*

class RestaurantListFragment : Fragment() {

    private lateinit var finderSharedViewModel: FinderSharedViewModel

    private lateinit var viewModel: RestaurantListViewModel

    private val fragmentDelegate: KatanaFragmentDelegate<RestaurantListFragment> = fragmentDelegate { activity, _ ->
        with((activity as KatanaTrait).component + listOf(RestaurantModule, RestaurantRepositoryModule)) {
            finderSharedViewModel = activityViewModelNow(this@fragmentDelegate)
            viewModel = viewModelNow(this@fragmentDelegate)
        }
    }

    /**
     *  Data handling specifically for RecyclerView (also possible for ViewPager2)
     */
    private val adapter = RestaurantListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.component_fragment_restaurantlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.let { nonNullContext ->

            // Makes RecyclerView use the restaurant adapter
            restaurantList.adapter = adapter

            // Wires a listener to the adapter which tells when a click
            // on a restaurant item happened.
            // For the moment shows a toast, might open a detail view later
            adapter.onRestaurantClickListener = { restaurant ->
                Toast.makeText(nonNullContext, "Clicked: ${restaurant.name}", Toast.LENGTH_SHORT).show()
            }

            // Handler for when "swipe refresh" gesture was done.
            restaurantListSwipeRefresh.setOnRefreshListener {
                viewModel.load(true)
            }

            // Handler for floating action button
            fab.setOnClickListener {
                findNavController().navigate(R.id.action_restaurantListFragment_to_finderFragment)
            }

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentDelegate.onActivityCreated(savedInstanceState)

        // Makes handleDataAvailable being called whenever new values
        // are written into restaurantLiveData.
        // By definition runs on the UI-thread
        viewModel.state.observe(
                this,
                Observer {
                    onStateUpdated(it)
                })

        // Final step:
        // Automatic data load upon opening of the activity.
        viewModel.load()
    }

    private fun onStateUpdated(state: State) {
        when (state) {
            State.Initial -> Unit
            is State.Loading -> bindViewForLoading(state.isRetryOrInitial)
            is State.Content -> bindViewForContent(state.list)
            is State.Error -> bindViewForError()
        }
    }

    private fun bindViewForLoading(isInitialOrRetry: Boolean) {
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

    private fun bindViewForContent(restaurants: List<Restaurant>) {
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

    private fun bindViewForError() {
        // Sets up error view
        progress.visibility = View.GONE

        errorLayout.visibility = View.VISIBLE
        retryButton.setOnClickListener { viewModel.load() }

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

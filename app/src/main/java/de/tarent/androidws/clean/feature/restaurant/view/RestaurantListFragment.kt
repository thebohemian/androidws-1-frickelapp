package de.tarent.androidws.clean.feature.restaurant.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.core.extension.observe
import de.tarent.androidws.clean.core.extension.observeEvent
import de.tarent.androidws.clean.feature.qrscanner.viewmodel.FinderSharedViewModel
import de.tarent.androidws.clean.feature.restaurant.injection.RestaurantModule
import de.tarent.androidws.clean.feature.restaurant.model.RestaurantItem
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel.Event
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel.State
import de.tarent.androidws.clean.repository.restaurant.injection.RestaurantRepositoryModule
import kotlinx.android.synthetic.main.component_fragment_restaurantlist.*
import org.rewedigital.katana.KatanaTrait
import org.rewedigital.katana.android.fragment.KatanaFragmentDelegate
import org.rewedigital.katana.android.fragment.fragmentDelegate
import org.rewedigital.katana.androidx.viewmodel.activityViewModelNow
import org.rewedigital.katana.androidx.viewmodel.viewModelNow

class RestaurantListFragment : Fragment() {

    private lateinit var finderSharedViewModel: FinderSharedViewModel

    private lateinit var viewModel: RestaurantListViewModel

    private val fragmentDelegate: KatanaFragmentDelegate<RestaurantListFragment> = fragmentDelegate { activity, _ ->
        with((activity as KatanaTrait).component + listOf(RestaurantModule, RestaurantRepositoryModule)) {
            finderSharedViewModel = activityViewModelNow(this@fragmentDelegate)
            viewModel = viewModelNow(this@fragmentDelegate)
        }
    }

    private val adapter = RestaurantListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.component_fragment_restaurantlist, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentDelegate.onActivityCreated(savedInstanceState)

        restaurantList.adapter = adapter

        // Wires a listener to the adapter which tells when a click
        // on a restaurant item happened.
        // For the moment shows a toast, might open a detail view later
        adapter.onRestaurantClickListener = ::onRestaurantItemClick

        // Handler for when "swipe refresh" gesture was done.
        restaurantListSwipeRefresh.setOnRefreshListener {
            viewModel.load(true)
        }

        // Handler for floating action button
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_restaurantListFragment_to_finderFragment)
        }

        viewLifecycleOwner.apply {
            // Makes handleDataAvailable being called whenever new values
            // are written into restaurantLiveData.
            // By definition runs on the UI-thread
            observe(viewModel.state, ::onStateUpdated)
            observeEvent(viewModel.event, ::onEvent)

            observeEvent(finderSharedViewModel.nameEvent, ::onNameEvent)
        }

        // Final step:
        // Automatic data load upon opening of the view.
        viewModel.load()
    }

    private fun onNameEvent(name: String) {
        Log.d(TAG, "name event: ${name}")
        viewModel.tryLookup(name)
    }

    private fun onStateUpdated(state: State) {
        when (state) {
            State.Initial -> Unit
            is State.Loading -> bindViewForLoading(state.isRetryOrInitial)
            is State.Content -> bindViewForContent(state.list)
            State.NetworkError -> bindViewForNetworkError()
            State.GeneralError -> bindViewForGeneralError()
        }
    }

    private fun onEvent(event: Event) {
        when (event) {
            is Event.LookedUp -> handleLookedUpEvent(event.index, event.item)
            is Event.LookUpFailed -> handleLookUpFailedEvent(event.name)
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

    private fun bindViewForContent(items: List<RestaurantItem>) {
        // Hides progress animation
        progress.visibility = View.GONE

        // Hides error views
        errorLayout.visibility = View.GONE
        retryButton.setOnClickListener(null)

        // Makes restaurant list visible and stops swipe refresh animation
        restaurantListSwipeRefresh.visibility = View.VISIBLE
        restaurantListSwipeRefresh.isRefreshing = false
        restaurantList.isEnabled = true
        adapter.submitList(items)
    }

    private fun bindViewForNetworkError() {
        // Sets up error view
        progress.visibility = View.GONE

        errorLayout.visibility = View.VISIBLE
        errorImageView.setImageResource(R.drawable.ic_network_error_48dp)
        errorMessageView.text = getString(R.string.networkerror_message)
        retryButton.setOnClickListener { viewModel.load() }

        restaurantListSwipeRefresh.visibility = View.GONE
        restaurantListSwipeRefresh.isRefreshing = false
        adapter.submitList(emptyList())
    }

    private fun bindViewForGeneralError() {
        // Sets up error view
        progress.visibility = View.GONE

        errorLayout.visibility = View.VISIBLE
        errorImageView.setImageResource(R.drawable.ic_general_error_48dp)
        errorMessageView.text = getString(R.string.generalerror_message)
        retryButton.setOnClickListener { viewModel.load() }

        restaurantListSwipeRefresh.visibility = View.GONE
        restaurantListSwipeRefresh.isRefreshing = false
        adapter.submitList(emptyList())
    }

    private fun handleLookedUpEvent(index: Int, item: RestaurantItem) {
        onRestaurantItemClick(item)
    }

    private fun onRestaurantItemClick(item: RestaurantItem) {
        context?.let { nonNullContext ->
            Toast.makeText(nonNullContext, "Clicked: ${item.restaurant.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLookUpFailedEvent(name: String) {
        Snackbar.make(rootLayout, getString(R.string.restaurant_not_found, name), Snackbar.LENGTH_SHORT)
                .show()
    }

    companion object {
        private const val TAG = "RestaurantListFrag"
    }
}

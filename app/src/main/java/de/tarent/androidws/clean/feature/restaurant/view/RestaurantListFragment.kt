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
import de.tarent.androidws.clean.feature.restaurant.view.binder.RestaurantListViewStateBinder
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

    private lateinit var binder: RestaurantListViewStateBinder

    private val fragmentDelegate: KatanaFragmentDelegate<RestaurantListFragment> = fragmentDelegate { activity, _ ->
        with((activity as KatanaTrait).component + listOf(RestaurantModule, RestaurantRepositoryModule)) {
            finderSharedViewModel = activityViewModelNow(this@fragmentDelegate)
            viewModel = viewModelNow(this@fragmentDelegate)
            binder = injectNow()
        }
    }

    private val adapter = RestaurantListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.component_fragment_restaurantlist, container, false)

    private fun views() = RestaurantListViewStateBinder.Views(
            progressBar = progress,
            errorImageView = errorImageView,
            errorLayout = errorLayout,
            errorMessageView = errorMessageView,
            retryButton = retryButton,
            swipeRefreshLayout = restaurantListSwipeRefresh,
            restaurantList = restaurantList,
            restaurantListAdapter = adapter,
            fab = fab
    )

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentDelegate.onActivityCreated(savedInstanceState)

        binder(
                views = views(),
                initialParams = RestaurantListViewStateBinder.InitialParams(
                        onRestaurantClickListener = ::onRestaurantItemClick,
                        onRestaurantCheckClickListener = ::onRestaurantItemCheckClick,
                        onRefreshAction = ::onSwipeRefresh,
                        onGoToFinderAction = ::onGoToFinderClick
                )
        )

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
        binder(
                views = views(),
                state = state,
                params = RestaurantListViewStateBinder.Params(
                        networkErrorResourceId = R.drawable.ic_network_error_48dp,
                        networkErrorMessage = getString(R.string.networkerror_message),
                        generalErrorResourceId = R.drawable.ic_general_error_48dp,
                        generalErrorMessage = getString(R.string.generalerror_message),
                        retryButtonAction = ::onRetryClick
                )
        )
    }

    private fun onEvent(event: Event) {
        when (event) {
            is Event.LookedUp -> handleLookedUpEvent(event.index, event.item)
            is Event.LookUpFailed -> handleLookUpFailedEvent(event.name)
        }
    }

    private fun handleLookUpFailedEvent(name: String) {
        Snackbar.make(rootLayout, getString(R.string.restaurant_not_found, name), Snackbar.LENGTH_SHORT)
                .show()
    }

    private fun handleLookedUpEvent(index: Int, item: RestaurantItem) {
        onRestaurantItemClick(item)
    }

    private fun onRetryClick() {
        viewModel.load()
    }

    private fun onGoToFinderClick() {
        findNavController().navigate(R.id.action_restaurantListFragment_to_finderFragment)
    }

    private fun onSwipeRefresh() {
        viewModel.load(true)
    }

    private fun onRestaurantItemClick(item: RestaurantItem) {
        context?.let { nonNullContext ->
            Toast.makeText(nonNullContext, "Clicked: ${item.restaurant.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRestaurantItemCheckClick(item: RestaurantItem) {
        context?.let { nonNullContext ->
            viewModel.toggleChecked(item)
        }
    }

    companion object {
        private const val TAG = "RestaurantListFrag"
    }
}

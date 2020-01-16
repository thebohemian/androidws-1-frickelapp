package de.tarent.androidws.clean.feature.restaurant.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.core.extension.observe
import de.tarent.androidws.clean.feature.restaurant.injection.RestaurantModule
import de.tarent.androidws.clean.feature.restaurant.model.RestaurantItem
import de.tarent.androidws.clean.feature.restaurant.view.binder.RestaurantListViewStateBinder
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel.State
import de.tarent.androidws.clean.repository.restaurant.injection.RestaurantRepositoryModule
import kotlinx.android.synthetic.main.component_fragment_restaurantlist.*
import org.rewedigital.katana.KatanaTrait
import org.rewedigital.katana.android.fragment.KatanaFragmentDelegate
import org.rewedigital.katana.android.fragment.fragmentDelegate
import org.rewedigital.katana.androidx.viewmodel.viewModelNow

class RestaurantListFragment : Fragment() {

    private lateinit var viewModel: RestaurantListViewModel

    private lateinit var binder: RestaurantListViewStateBinder

    private val fragmentDelegate: KatanaFragmentDelegate<RestaurantListFragment> = fragmentDelegate { activity, _ ->
        with((activity as KatanaTrait).component + listOf(RestaurantModule, RestaurantRepositoryModule)) {
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
            restaurantListAdapter = adapter
    )

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentDelegate.onActivityCreated(savedInstanceState)

        binder(
                views = views(),
                initialParams = RestaurantListViewStateBinder.InitialParams(
                        onRestaurantClickAction = ::onRestaurantItemClick,
                        onRestaurantCheckClickAction = ::onRestaurantItemCheckClick,
                        onRefreshAction = ::onSwipeRefresh
                )
        )

        viewLifecycleOwner.apply {
            observe(viewModel.state, ::onStateUpdated)
        }

        // Automatic data load upon opening of the view.
        viewModel.load()
    }

    private fun onNameEvent(name: String) {
        Log.d(TAG, "name event: ${name}")
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

    private fun onRetryClick() {
        viewModel.load()
    }

    private fun onSwipeRefresh() {
        viewModel.load(true)
    }

    private fun onRestaurantItemClick(item: RestaurantItem) {
        // Can be used if needed
    }

    private fun onRestaurantItemCheckClick(item: RestaurantItem) {
        // Can be used if needed
    }

    companion object {
        private const val TAG = "RestaurantListFrag"
    }
}

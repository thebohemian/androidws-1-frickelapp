package de.tarent.androidws.clean.feature.restaurant.view.binder

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.tarent.androidws.clean.core.extension.toOnClickListener
import de.tarent.androidws.clean.core.viewmodel.Action
import de.tarent.androidws.clean.feature.restaurant.view.OnRestaurantClickListener
import de.tarent.androidws.clean.feature.restaurant.view.RestaurantListAdapter
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel.State

internal class RestaurantListViewStateBinder {

    data class Views(
            val progressBar: ProgressBar,
            val errorLayout: ViewGroup,
            val errorImageView: ImageView,
            val errorMessageView: TextView,
            val retryButton: Button,
            val swipeRefreshLayout: SwipeRefreshLayout,
            val restaurantList: RecyclerView,
            val restaurantListAdapter: RestaurantListAdapter,
            val fab: FloatingActionButton)

    data class Params(
            @DrawableRes val networkErrorResourceId: Int,
            val networkErrorMessage: String,
            @DrawableRes val generalErrorResourceId: Int,
            val generalErrorMessage: String,
            val retryButtonAction: Action
    )

    data class InitialParams(
            val onRestaurantClickListener: OnRestaurantClickListener,
            val onRestaurantCheckClickListener: OnRestaurantClickListener,
            val onRefreshAction: Action,
            val onGoToFinderAction: Action
    )

    operator fun invoke(views: Views, initialParams: InitialParams) {
        with(views) {
            restaurantList.adapter = restaurantListAdapter

            with(restaurantListAdapter) {
                onRestaurantClickListener = initialParams.onRestaurantClickListener
                onRestaurantCheckClickListener = initialParams.onRestaurantCheckClickListener
            }

            swipeRefreshLayout.setOnRefreshListener(initialParams.onRefreshAction)

            fab.setOnClickListener(initialParams.onGoToFinderAction.toOnClickListener())
        }
    }

    operator fun invoke(views: Views, state: State, params: Params) {
        when (state) {
            is State.Initial -> bindStateInitial(views)
            is State.Loading -> bindStateLoading(views, state)
            is State.Content -> bindStateContent(views, state)
            is State.NetworkError -> bindStateNetworkError(views, params)
            is State.GeneralError -> bindStateGeneralError(views, params)
        }
    }

    private fun bindStateInitial(views: Views) = Unit

    private fun bindStateLoading(views: Views, loading: State.Loading) {
        with(views) {
            if (loading.isRetryOrInitial) {
                // Plays progress animation
                progressBar.visibility = View.VISIBLE

                // Makes restaurant list and swiperfresh stuff invisible
                swipeRefreshLayout.visibility = View.GONE
            } else {
                // Just disable interaction with restaurant list
                restaurantList.isEnabled = false
            }

            errorLayout.visibility = View.GONE
            retryButton.setOnClickListener(null)
        }
    }

    private fun bindStateContent(views: Views, content: State.Content) {
        with(views) {
            // Hides progress animation
            progressBar.visibility = View.GONE

            // Hides error views
            errorLayout.visibility = View.GONE
            retryButton.setOnClickListener(null)

            // Makes restaurant list visible and stops swipe refresh animation
            swipeRefreshLayout.visibility = View.VISIBLE
            swipeRefreshLayout.isRefreshing = false
            restaurantList.isEnabled = true
            restaurantListAdapter.submitList(content.list)
        }
    }

    private fun bindStateNetworkError(views: Views, params: Params) {
        with(views) {
            // Sets up error view
            progressBar.visibility = View.GONE

            errorLayout.visibility = View.VISIBLE
            errorImageView.setImageResource(params.networkErrorResourceId)
            errorMessageView.text = params.networkErrorMessage
            retryButton.setOnClickListener(params.retryButtonAction.toOnClickListener())

            swipeRefreshLayout.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
            restaurantListAdapter.submitList(emptyList())

        }
    }

    private fun bindStateGeneralError(views: Views, params: Params) {
        with(views) {
            // Sets up error view
            progressBar.visibility = View.GONE

            errorLayout.visibility = View.VISIBLE
            errorImageView.setImageResource(params.generalErrorResourceId)
            errorMessageView.text = params.generalErrorMessage
            retryButton.setOnClickListener(params.retryButtonAction.toOnClickListener())

            swipeRefreshLayout.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
            restaurantListAdapter.submitList(emptyList())
        }
    }

}
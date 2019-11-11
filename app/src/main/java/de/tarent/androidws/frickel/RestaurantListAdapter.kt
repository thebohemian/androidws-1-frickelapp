package de.tarent.androidws.frickel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.component_restaurant_item.view.*

/** External interface for handling clicks */
typealias OnRestaurantClickListener = (Restaurant) -> Unit

class RestaurantListAdapter : ListAdapter<Restaurant, RestaurantViewHolder>(DiffCallback()) {

    var onRestaurantClickListener: OnRestaurantClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RestaurantViewHolder.create(parent)

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        // TODO: Also bind clicking
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return oldItem == newItem
        }
    }

    /* TODO: Maybe have a separate function which creates the listener and the listener
    * has all needed functionality already
    private fun newOnClickListener(<some args>) = ...
    */

}

class RestaurantViewHolder private constructor(private val rootView: View)
    : RecyclerView.ViewHolder(rootView) {

    fun bind(restaurant: Restaurant) {
        with(rootView) {
            // TODO: get a click listener from somewhere and wire it to our card
            //restaurantCard.setOnClickListener(...)
            restaurantName.text = restaurant.name
            restaurantImage.loadUrl(restaurant.presentationImage)
        }
    }

    companion object {
        fun create(parent: ViewGroup) =
                RestaurantViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.component_restaurant_item, parent, false))
    }

}
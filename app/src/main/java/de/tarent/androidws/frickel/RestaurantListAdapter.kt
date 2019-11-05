package de.tarent.androidws.frickel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.component_restaurant_item.view.*

class RestaurantListAdapter : ListAdapter<Restaurant, RestaurantViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RestaurantViewHolder.create(parent)

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
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

}

class RestaurantViewHolder private constructor(private val rootView: View)
    : RecyclerView.ViewHolder(rootView) {

    fun bind(restaurant: Restaurant) {
        with(rootView) {
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
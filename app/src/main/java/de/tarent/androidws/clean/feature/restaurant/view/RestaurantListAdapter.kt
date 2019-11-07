package de.tarent.androidws.clean.feature.restaurant.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.core.images.extensions.loadUrl
import de.tarent.androidws.clean.repository.restaurant.model.Restaurant
import kotlinx.android.synthetic.main.component_restaurant_item.view.*

internal typealias OnRestaurantClickListener = (Restaurant) -> Unit

internal class RestaurantListAdapter : ListAdapter<Restaurant, RestaurantViewHolder>(DiffCallback()) {

    var onRestaurantClickListener: OnRestaurantClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RestaurantViewHolder.create(parent)

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(getItem(position), View.OnClickListener {
            onClick(position)
        })
    }

    private class DiffCallback : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return oldItem == newItem
        }
    }

    private fun onClick(position: Int) {
        onRestaurantClickListener?.let { it(getItem(position)) }
    }

}

class RestaurantViewHolder private constructor(private val rootView: View)
    : RecyclerView.ViewHolder(rootView) {

    fun bind(restaurant: Restaurant, onClicked: View.OnClickListener) {
        with(rootView) {
            restaurantCard.setOnClickListener(onClicked)
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
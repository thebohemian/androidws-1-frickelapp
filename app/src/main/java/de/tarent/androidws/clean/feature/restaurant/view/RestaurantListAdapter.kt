package de.tarent.androidws.clean.feature.restaurant.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.core.images.extensions.loadUrl
import de.tarent.androidws.clean.feature.restaurant.model.RestaurantItem
import kotlinx.android.synthetic.main.component_restaurant_item.view.*

internal typealias OnRestaurantClickListener = (RestaurantItem) -> Unit

internal class RestaurantListAdapter : ListAdapter<RestaurantItem, RestaurantItemViewHolder>(DiffCallback()) {

    var onRestaurantClickListener: OnRestaurantClickListener? = null

    var onRestaurantCheckClickListener: OnRestaurantClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RestaurantItemViewHolder.create(parent)

    override fun onBindViewHolder(holder: RestaurantItemViewHolder, position: Int) {
        getItem(position).let { item ->
            holder.bind(
                    item = item,
                    onClicked = newOnClickListener(item),
                    onCheckClicked = newOnCheckClickListener(item)
            )
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RestaurantItem>() {
        override fun areItemsTheSame(oldItem: RestaurantItem, newItem: RestaurantItem): Boolean {
            return oldItem.restaurant.name == newItem.restaurant.name
        }

        override fun areContentsTheSame(oldItem: RestaurantItem, newItem: RestaurantItem): Boolean {
            return oldItem == newItem
        }
    }

    private fun newOnClickListener(item: RestaurantItem) = View.OnClickListener {
        onRestaurantClickListener?.invoke(item)
    }

    private fun newOnCheckClickListener(item: RestaurantItem) = View.OnClickListener {
        onRestaurantCheckClickListener?.invoke(item)
    }

}

internal class RestaurantItemViewHolder private constructor(private val rootView: View)
    : RecyclerView.ViewHolder(rootView) {

    fun bind(item: RestaurantItem, onClicked: View.OnClickListener, onCheckClicked: View.OnClickListener) {
        with(rootView) {
            restaurantCard.setOnClickListener(onClicked)
            restaurantName.text = item.restaurant.name
            restaurantImage.loadUrl(item.restaurant.presentationImage)
            restaurantCheckBox.isChecked = item.checked
            restaurantCheckBox.setOnClickListener(onCheckClicked)
        }
    }

    companion object {
        fun create(parent: ViewGroup) =
                RestaurantItemViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.component_restaurant_item, parent, false))
    }

}
package de.tarent.androidws.clean

import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

fun ImageView.loadUrl(url: String, placeholder: Int? = null) {
    GlideApp
            .with(context)
            .load(url)
            .apply {
                placeholder?.let {
                    apply(RequestOptions.errorOf(it)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(it))
                }
            }
            .into(this)

}
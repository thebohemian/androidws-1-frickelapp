package de.tarent.androidws.clean.core.images.extensions

import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.tarent.androidws.clean.core.images.GlideApp

fun ImageView.loadUrl(url: String, placeholder: Int? = null) {
    GlideApp.with(context)
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
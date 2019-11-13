package de.tarent.androidws.clean.core.extension

import android.view.View
import de.tarent.androidws.clean.core.viewmodel.Action

fun Action.toOnClickListener() = View.OnClickListener { this() }
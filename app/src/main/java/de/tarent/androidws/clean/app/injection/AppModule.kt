package de.tarent.androidws.clean.app.injection

import android.content.Context
import android.content.res.Resources
import org.rewedigital.katana.Module
import org.rewedigital.katana.android.modules.APPLICATION_CONTEXT
import org.rewedigital.katana.dsl.get
import org.rewedigital.katana.dsl.singleton

val AppModule = Module {

    singleton<Resources> { get<Context>(APPLICATION_CONTEXT).resources }
}

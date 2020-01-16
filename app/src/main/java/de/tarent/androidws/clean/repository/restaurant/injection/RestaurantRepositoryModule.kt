package de.tarent.androidws.clean.repository.restaurant.injection

import android.content.res.Resources
import de.tarent.androidws.clean.R
import de.tarent.androidws.clean.repository.restaurant.remote.RestaurantsRemote
import de.tarent.androidws.clean.repository.restaurant.repository.RestaurantRepository
import de.tarent.androidws.clean.repository.restaurant.repository.RestaurantRepositoryImpl
import org.rewedigital.katana.Module
import org.rewedigital.katana.dsl.factory
import org.rewedigital.katana.dsl.get

private enum class Names { BASE_URL }

val RestaurantRepositoryModule = Module {

    factory<RestaurantRepository> { RestaurantRepositoryImpl(get()) }

    factory {
        RestaurantsRemote.create(get(), get(Names.BASE_URL))
    }

    factory(Names.BASE_URL) { get<Resources>().getString(R.string.service_url) }

}
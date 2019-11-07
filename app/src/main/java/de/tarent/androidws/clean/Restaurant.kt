package de.tarent.androidws.clean

data class Restaurant(
        val name: String,
        val presentationImage: String,
        val kitchenTypes: List<KitchenType>,
        val openingHours: List<String>,
        val location: Location) {

    enum class KitchenType {
        chinese, thai, italian, american, german
    }

    data class Location(
            val lon: String,
            val lat: String)
}

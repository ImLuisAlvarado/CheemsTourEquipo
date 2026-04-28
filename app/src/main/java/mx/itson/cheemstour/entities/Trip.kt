package mx.itson.cheemstour.entities

class Trip {
    val id: Int
    val name: String
    val city: String
    val latitude: Double
    val longitude: Double

    constructor(id: Int, name: String, city: String, latitude: Double, longitude: Double) {
        this.id = id
        this.name = name
        this.city = city
        this.latitude = latitude
        this.longitude = longitude
    }


}
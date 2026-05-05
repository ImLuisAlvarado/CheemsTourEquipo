package mx.itson.cheemstour.entities

class Trip {
    var id: Int = 0
    var name: String = ""
    var city: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor()

    constructor(id: Int, name: String, city: String, latitude: Double, longitude: Double) {
        this.id = id
        this.name = name
        this.city = city
        this.latitude = latitude
        this.longitude = longitude
    }


}
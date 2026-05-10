package mx.itson.cheemstour.entities

import com.google.gson.annotations.SerializedName

class WeatherTemperature {

    @SerializedName("temp")
    var temperature: Double = 0.0

    @SerializedName("temp_min")
    var temperatureMin: Double = 0.0

    @SerializedName("temp_max")
    var temperatureMax: Double = 0.0

    @SerializedName("humidity")
    var humidity: Int = 0

    @SerializedName("feels_like")
    var feel: Double = 0.0





}
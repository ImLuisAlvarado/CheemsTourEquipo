package mx.itson.cheemstour.entities

import com.google.gson.annotations.SerializedName


class Weather {
    @SerializedName("name")
    var city: String = ""

    @SerializedName("main")
    var temperature: WeatherTemperature = WeatherTemperature()

    @SerializedName("weather")
    var description: List<WeatherDescription> = emptyList()

    @SerializedName("sys")
    var sun: WeatherSun = WeatherSun()
}
package mx.itson.cheemstour.entities

import com.google.gson.annotations.SerializedName

class WeatherSun {

    @SerializedName("sunrise")
    var sunrise: Long = 0L

    @SerializedName("sunset")
    var sunset: Long = 0L

}
package mx.itson.cheemstour.entities

import com.google.gson.annotations.SerializedName

class WeatherDescription {

    @SerializedName("description")
    var description: String = ""

    @SerializedName("icon")
    var icon: String? = null

}
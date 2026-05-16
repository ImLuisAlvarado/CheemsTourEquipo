package mx.itson.cheemstour.utils

import com.google.gson.GsonBuilder
import mx.itson.cheemstour.interfaces.CheemsAPI
import mx.itson.cheemstour.interfaces.OpenWeatherAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitUtil {
    fun getApiCheems() : CheemsAPI {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://cheemsgo.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create(CheemsAPI::class.java)
    }

    fun getApiWeather() : OpenWeatherAPI{
        val gson = GsonBuilder().create()

        val retrofit = Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/").addConverterFactory(
            GsonConverterFactory.create(gson)).build()

        return retrofit.create(OpenWeatherAPI::class.java)
    }
}
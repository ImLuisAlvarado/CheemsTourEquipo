package mx.itson.cheemstour.utils

import com.google.gson.GsonBuilder
import mx.itson.cheemstour.interfaces.CheemsAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitUtil {
    fun getApi() : CheemsAPI {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.100.46:5001/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create(CheemsAPI::class.java)
    }
}
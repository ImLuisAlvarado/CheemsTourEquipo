package mx.itson.cheemstour.interfaces

import mx.itson.cheemstour.entities.Trip
import retrofit2.Call
import retrofit2.http.GET

interface CheemsAPI {

    @GET("trips")
    fun getTrips() : Call<List<Trip>>
}
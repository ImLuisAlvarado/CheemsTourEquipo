package mx.itson.cheemstour.interfaces

import mx.itson.cheemstour.entities.ApiResponse
import mx.itson.cheemstour.entities.Trip
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CheemsAPI {

    @GET("trips")
    fun getTrips() : Call<List<Trip>>

    @POST("trip")
    fun saveTrip(@Body trip: Trip) : Call<ApiResponse>
}
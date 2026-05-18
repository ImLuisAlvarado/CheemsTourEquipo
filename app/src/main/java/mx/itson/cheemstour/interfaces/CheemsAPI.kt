package mx.itson.cheemstour.interfaces

import mx.itson.cheemstour.entities.ApiResponse
import mx.itson.cheemstour.entities.Trip
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CheemsAPI {

    @GET("trips")
    fun getTrips() : Call<List<Trip>>

    @POST("trip")
    fun saveTrip(@Body trip: Trip) : Call<ApiResponse>

    @PUT("trips/{id}")
    fun updateTrip(@Path("id") id: Int, @Body trip: Trip): Call<ApiResponse>

    @DELETE("trips/{id}")
    fun deleteTrip(@Path("id") id: Int): Call<ApiResponse>
}
package mx.itson.cheemstour

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import mx.itson.cheemstour.entities.Trip
import mx.itson.cheemstour.entities.Weather
import mx.itson.cheemstour.utils.RetrofitUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripMapActivity : AppCompatActivity(), OnMapReadyCallback {

    var map : GoogleMap? = null
    val markerTrip = HashMap<String, Trip>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trip_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
    fun getTrips() {
        val call: Call<List<Trip>> = RetrofitUtil.getApiCheems().getTrips()
        call.enqueue(object : Callback<List<Trip>> {
            override fun onResponse(
                call: Call<List<Trip>>,
                response: Response<List<Trip>>) {
                val trips: List<Trip> = response.body()!!

                trips.forEach { t->
                    val latLng = LatLng(t.latitude, t.longitude)

                    val marker = map?.addMarker(
                        MarkerOptions().
                        position(latLng).
                        title(t.name).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.cheems))
                    )

                    // guardamos el trip asociado a su marker
                    if(marker != null){
                        markerTrip[marker.id] = t
                    }
                }
            }

            override fun onFailure(call: Call<List<Trip>>, t: Throwable) {
                Log.e("API_FAILURE", "Fallo la comunicación con la API: ${t.message}", t)
            }
        })
    }


    fun getWeather(trip: Trip) {
        val call: Call<Weather> = RetrofitUtil.getApiWeather().getWeather(
            lat   = trip.latitude,
            lon   = trip.longitude,
            appid = "6c79d81c72b4a32706697c4485a9a8a1",
            units = "metric"
        )
        call.enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                val weather: Weather = response.body()!!
                Log.d("Weather city", weather.city)
                showTripInfo(trip, weather)
            }

            override fun onFailure(call: Call<Weather>, t: Throwable) {
                Log.e("Error calling API", t.message.toString())
            }
        })
    }


    fun showTripInfo(trip: Trip, weather: Weather){
        val temperature = weather.temperature.temperature.toInt()
        val temperatureMin = weather.temperature.temperatureMin.toInt()
        val description = weather.description.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "--"
        val date = Date(weather.sunrise.sunrise * 1000L)
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val view = layoutInflater.inflate(R.layout.dialog_trip_info, null)

        view.findViewById<TextView>(R.id.dialog_trip_name).text = trip.name
        view.findViewById<TextView>(R.id.dialog_trip_city).text = "Ciudad: ${trip.city}"
        view.findViewById<TextView>(R.id.dialog_weather_temp).text = "$temperature°C"
        view.findViewById<TextView>(R.id.dialog_weather_temp_min).text = "Temperatura minima: $temperatureMin°C"
        view.findViewById<TextView>(R.id.dialog_weather_desc).text = description
        view.findViewById<TextView>(R.id.dialog_weather_humidity).text = "Humedad: ${weather.temperature.humidity}%"
        view.findViewById<TextView>(R.id.dialog_weather_sunrise).text = "${dateFormat.format(date)}"

        view.findViewById<LinearLayout>(R.id.main).setBackgroundColor(
            when {
                temperature <= 15 -> android.graphics.Color.parseColor("#2196F3")
                temperature <= 28 -> android.graphics.Color.parseColor("#FFC107")
                else              -> android.graphics.Color.parseColor("#FF5722")
            }
        )

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()

    }





    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap
            map!!.mapType = GoogleMap.MAP_TYPE_NORMAL

            map!!.setOnMarkerClickListener{ marker ->
                val trip = markerTrip[marker.id]
                if(trip != null){
                    getWeather(trip)
                }
                true

            }

            getTrips()
        }catch (ex : Exception){
            Log.e( "Error loading map", ex.message ?: "Ocurrió un error desconocido")
        }
    }
}
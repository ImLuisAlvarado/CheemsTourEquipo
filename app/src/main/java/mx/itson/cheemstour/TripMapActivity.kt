package mx.itson.cheemstour

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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
import mx.itson.cheemstour.entities.ApiResponse
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
    // siclo de vida para actializar el mapa despues de un cambio
    override fun onResume() {
        super.onResume()
        // al actualizar un trip el mapa se tiene que actualizar
        if (map != null) {
            map?.clear()
            markerTrip.clear()
            getTrips()
        }
    }

    private fun confirmDeleteTrip(trip: Trip) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Viaje")
            .setMessage("¿Estás seguro de que deseas eliminar a ${trip.name}?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                val call = RetrofitUtil.getApiCheems().deleteTrip(trip.id!!)
                call.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            map?.clear()
                            markerTrip.clear()
                            getTrips()
                        } else {
                            Log.e("API_ERROR", "Fallo al eliminar")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("API_FAILURE", "Error de red: ${t.message}")
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openEditScreen(trip: Trip) {
        val intent = android.content.Intent(this, FromsTripMapsActivity::class.java)

        intent.putExtra("id", trip.id)
        intent.putExtra("name", trip.name)
        intent.putExtra("city", trip.city)
        intent.putExtra("lat", trip.latitude)
        intent.putExtra("lng", trip.longitude)

        startActivity(intent)
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
        val lang = when (Locale.getDefault().language) {
            "es" -> "es"
            else -> "en"
        }

        val call: Call<Weather> = RetrofitUtil.getApiWeather().getWeather(
            lat   = trip.latitude,
            lon   = trip.longitude,
            appid = BuildConfig.OPENWEATHER_API_KEY,
            units = "metric",
            lang  = lang
        )
        call.enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!
                    Log.d("Weather city", weather.city ?: "Ciudad desconocida")
                    showTripInfo(trip, weather)
                } else {
                    Log.e("API_ERROR", "El servidor no devolvió el clima. Código: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Weather>, t: Throwable) {
                Log.e("Error calling API", t.message ?: "Error desconocido de red")
            }
        })
    }


    fun showTripInfo(trip: Trip, weather: Weather){
        val temperature = weather.temperature?.temperature?.toInt() ?: 0
        val temperatureMin = weather.temperature?.temperatureMin?.toInt() ?: 0
        val temperatureMax = weather.temperature?.temperatureMax?.toInt() ?: 0
        val description = weather.description?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "--"

        val sunRiseUnix = weather.sun?.sunrise ?: 0L
        val sunSetUnix = weather.sun?.sunset ?: 0L
        val dateSunrise = Date(sunRiseUnix * 1000L)
        val dateSunset = Date(sunSetUnix * 1000L)
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val feel = weather.temperature?.feel?.toInt() ?: 0
        val humidity = weather.temperature?.humidity ?: 0

        val view = layoutInflater.inflate(R.layout.dialog_trip_info, null)

        view.findViewById<TextView>(R.id.dialog_trip_name).text = trip.name
        view.findViewById<TextView>(R.id.dialog_trip_city).text = "Ciudad: ${trip.city}"
        view.findViewById<TextView>(R.id.dialog_weather_temp).text = "$temperature°C"
        view.findViewById<TextView>(R.id.dialog_weather_temp_min).text = "Temperatura minima: $temperatureMin°C"
        view.findViewById<TextView>(R.id.dialog_weather_temp_max).text = "Temperatura maxima: $temperatureMax°C"
        view.findViewById<TextView>(R.id.dialog_weather_feel).text = "Sensación térmica: $feel°C"
        view.findViewById<TextView>(R.id.dialog_weather_desc).text = description
        view.findViewById<TextView>(R.id.dialog_weather_humidity).text = "Humedad: $humidity%"
        view.findViewById<TextView>(R.id.dialog_weather_sunrise).text = "Salida del sol: ${dateFormat.format(dateSunrise)}"
        view.findViewById<TextView>(R.id.dialog_weather_sunset).text = "Puesta del sol: ${dateFormat.format(dateSunset)}"

        val imgWeatherIcon = view.findViewById<ImageView>(R.id.dialog_weather_icon)
        val iconCode = weather.description?.firstOrNull()?.icon

        if (iconCode != null) {
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@4x.png"
            com.bumptech.glide.Glide.with(this)
                .load(iconUrl)
                .into(imgWeatherIcon)
        }

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
            // botones para realizar cambios
            .setNeutralButton("Editar") { _, _ ->
                openEditScreen(trip)
            }
            .setNegativeButton("Eliminar") { _, _ ->
                confirmDeleteTrip(trip)
            }
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
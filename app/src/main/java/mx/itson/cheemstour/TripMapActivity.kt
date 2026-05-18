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
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import mx.itson.cheemstour.entities.Trip
import mx.itson.cheemstour.entities.Weather
import mx.itson.cheemstour.utils.RetrofitUtil
import mx.itson.cheemstour.utils.SunPathView
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
            override fun onResponse(call: Call<List<Trip>>, response: Response<List<Trip>>) {
                val trips: List<Trip> = response.body()!!
                trips.forEach { t->
                    val latLng = LatLng(t.latitude, t.longitude)
                    val marker = map?.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(t.name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cheems))
                    )
                    if(marker != null){
                        markerTrip[marker.id] = t
                    }
                }
            }
            override fun onFailure(call: Call<List<Trip>>, t: Throwable) {
                Log.e("API_FAILURE", "Fallo la comunicación: ${t.message}", t)
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
                    showTripInfo(trip, weather)
                } else {
                    Log.e("API_ERROR", "Código: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Weather>, t: Throwable) {
                Log.e("Error API", t.message ?: "Error desconocido")
            }
        })
    }

    fun showTripInfo(trip: Trip, weather: Weather){
        val temperature = weather.temperature?.temperature?.toInt() ?: 0
        val temperatureMin = weather.temperature?.temperatureMin?.toInt() ?: 0
        val temperatureMax = weather.temperature?.temperatureMax?.toInt() ?: 0
        val description = weather.description?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "--"
        val feel = weather.temperature?.feel?.toInt() ?: 0
        val humidity = weather.temperature?.humidity ?: 0

        // Zona horaria local
        val sunRiseUnix = weather.sun?.sunrise ?: 0L
        val sunSetUnix = weather.sun?.sunset ?: 0L
        val tzShift = weather.timezone
        val currentUnix = System.currentTimeMillis() / 1000L

        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")

        // Calculamos las fechas locales usando el desfase (tzShift)
        val dateSunrise = Date((sunRiseUnix + tzShift) * 1000L)
        val dateSunset = Date((sunSetUnix + tzShift) * 1000L)
        val dateCurrentLocal = Date((currentUnix + tzShift) * 1000L) // NUEVO: Hora local actual

        val isDay = currentUnix in sunRiseUnix..sunSetUnix

        val view = layoutInflater.inflate(R.layout.dialog_trip_info, null)

        view.findViewById<TextView>(R.id.dialog_trip_name).text = trip.name
        view.findViewById<TextView>(R.id.dialog_trip_city).text = "Ciudad: ${trip.city}"
        view.findViewById<TextView>(R.id.dialog_weather_temp).text = "$temperature°C"
        view.findViewById<TextView>(R.id.dialog_weather_temp_min).text = "Temperatura mínima: $temperatureMin°C"
        view.findViewById<TextView>(R.id.dialog_weather_temp_max).text = "Temperatura máxima: $temperatureMax°C"
        view.findViewById<TextView>(R.id.dialog_weather_feel).text = "Sensación térmica: $feel°C"
        view.findViewById<TextView>(R.id.dialog_weather_desc).text = description
        view.findViewById<TextView>(R.id.dialog_weather_humidity).text = "Humedad: $humidity%"

        // NUEVO: Asignamos la hora local formateada
        view.findViewById<TextView>(R.id.dialog_local_time).text = "Hora actual: ${dateFormat.format(dateCurrentLocal)}"

        val txtAmanecer = view.findViewById<TextView>(R.id.dialog_weather_sunrise)
        val txtAtardecer = view.findViewById<TextView>(R.id.dialog_weather_sunset)

        if (isDay) {
            txtAmanecer.text = "Amanecer\n${dateFormat.format(dateSunrise)}"
            txtAtardecer.text = "Atardecer\n${dateFormat.format(dateSunset)}"
        } else {
            txtAmanecer.text = "Atardecer\n${dateFormat.format(dateSunset)}"
            txtAtardecer.text = "Próx. Amanecer\n${dateFormat.format(dateSunrise)}"
        }

        val sunPathView = view.findViewById<SunPathView>(R.id.dialog_sun_path)
        sunPathView.setTimes(sunRiseUnix, sunSetUnix, currentUnix)

        val imgWeatherIcon = view.findViewById<ImageView>(R.id.dialog_weather_icon)
        val iconCode = weather.description?.firstOrNull()?.icon

        if (iconCode != null) {
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@4x.png"
            Glide.with(this).load(iconUrl).into(imgWeatherIcon)
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
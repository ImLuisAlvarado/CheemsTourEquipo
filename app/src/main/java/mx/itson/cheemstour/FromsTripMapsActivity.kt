package mx.itson.cheemstour

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import mx.itson.cheemstour.entities.ApiResponse
import mx.itson.cheemstour.entities.Trip
import mx.itson.cheemstour.utils.RetrofitUtil
import mx.itson.cheemstour.utils.VibratorUtil
import retrofit2.Call
import java.util.Locale

class FromsTripMapsActivity : AppCompatActivity(), View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    var map : GoogleMap? = null
    var selectedLatitude: Double = 0.0
    var selectedLongitude: Double = 0.0
    var selectedCity: String = ""
    var tripId: Int = 0 // si es 0 es un Trip nuevo, si tiene valor es un update


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_froms_trip_maps)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync (this)

        val btnSaveTrip = findViewById<View>(R.id.btn_save_trip) as Button
        btnSaveTrip.setOnClickListener(this)

        // captura de id para editar
        tripId = intent.getIntExtra("id", 0)
        // si el id es mayor a 0 se llenaran los campos
        if (tripId > 0) {
            val txfNameTrip = findViewById<EditText>(R.id.edit_text_name_trip)
            txfNameTrip.setText(intent.getStringExtra("name"))

            selectedCity = intent.getStringExtra("city") ?: ""
            selectedLatitude = intent.getDoubleExtra("lat", 0.0)
            selectedLongitude = intent.getDoubleExtra("lng", 0.0)

            btnSaveTrip.text = getString(R.string.update_trip)
        }
    }

    override fun onClick(view: View?) {
        try {
            if (view?.id == R.id.btn_save_trip){

                var txfNameTrip = findViewById<EditText>(R.id.edit_text_name_trip)
                var tripName = txfNameTrip.text.toString()

                if(tripName.isNotEmpty() && selectedLatitude != 0.0){
                    //// Empaquetamos los datos que el usuario escribio o selecciono
                    var newTrip = Trip()
                    newTrip.id = tripId
                    newTrip.name = tripName
                    newTrip.city = selectedCity
                    newTrip.latitude = selectedLatitude
                    newTrip.longitude = selectedLongitude

                    val call: Call<ApiResponse> = if (tripId > 0) {
                        RetrofitUtil.getApiCheems().updateTrip(tripId, newTrip) //PUT
                    } else {
                        RetrofitUtil.getApiCheems().saveTrip(newTrip) //POST
                    }

                    call.enqueue(object : retrofit2.Callback<ApiResponse> {

                        override fun onResponse(call: Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                            if (response.isSuccessful) {
                                var apiResponse = response.body()

                                if (apiResponse != null && apiResponse.success) {
                                    Toast.makeText(this@FromsTripMapsActivity, R.string.save_trip_toast, Toast.LENGTH_LONG).show()
                                    VibratorUtil.vibrate(this@FromsTripMapsActivity, 500)
                                    txfNameTrip.setText("")

                                    // Si editamos, regresamos al mapa directo. Si es nuevo, limpiamos la caja
                                    if (tripId > 0) {
                                        finish()
                                    }

                                } else {
                                    Toast.makeText(this@FromsTripMapsActivity, R.string.error_save_db, Toast.LENGTH_SHORT).show()
                                    VibratorUtil.vibrate(this@FromsTripMapsActivity, 1000)
                                }
                            } else {
                                Log.e("API_ERROR", "Error HTTP: ${response.code()}")
                                Toast.makeText(this@FromsTripMapsActivity, R.string.Failed_server, Toast.LENGTH_SHORT).show()
                                VibratorUtil.vibrate(this@FromsTripMapsActivity, 1000)
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Log.e("API_FAILURE", "Fallo la comunicación con la API: ${t.message}", t)
                            Toast.makeText(this@FromsTripMapsActivity, R.string.err_connection, Toast.LENGTH_SHORT).show()
                            VibratorUtil.vibrate(this@FromsTripMapsActivity, 1000)
                        }
                    })

                } else {
                    Toast.makeText(this, R.string.enter_name_select_point, Toast.LENGTH_LONG).show()
                    VibratorUtil.vibrate(this, 500)
                }
            }
        } catch (ex : Exception){
            Log.e("CLICK_ERROR", ex.message ?: "Error al procesar el clic del botón")
            Toast.makeText(this, R.string.err_click, Toast.LENGTH_SHORT).show()
            VibratorUtil.vibrate(this, 1000)

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap
            map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            map?.clear()
            // se decide donde poner el marcador inicial
            var latLng = if (tripId > 0) {
                LatLng(selectedLatitude, selectedLongitude)
            } else {
                LatLng(27.9179, -110.9089)
            }

            map!!.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.selected_trip))
                    .draggable(true)
            )

            map?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            map?.animateCamera(CameraUpdateFactory.zoomTo(10f))

            selectedLatitude = latLng.latitude
            selectedLongitude = latLng.longitude

            map?.setOnMarkerDragListener(this)

        } catch (ex : Exception){
            Log.e("Error loading map", ex.message ?: "Ocurrió un error desconocido")
            Toast.makeText(this, R.string.err_loading_map, Toast.LENGTH_SHORT).show()
            VibratorUtil.vibrate(this, 800)
        }
    }

    override fun onMarkerDragStart(marker: Marker) {
        Log.d("MAPA", "Inicia el arrastre del marcador")
        VibratorUtil.vibrate(this, 100)
    }

    override fun onMarkerDrag(marker: Marker) {

    }

    override fun onMarkerDragEnd(marker: Marker) {
        try {
            // Cuando el usuario suelta el marcador, actualizamos las coordenadas
            var newPosition = marker.position
            selectedLatitude = newPosition.latitude
            selectedLongitude = newPosition.longitude

            Log.d("MAPA", "Marcador soltado en Lat: $selectedLatitude, Lng: $selectedLongitude")
            VibratorUtil.vibrate(this, 200)

            var geocoder = Geocoder(this, Locale.getDefault())
            var addresses = geocoder.getFromLocation(selectedLatitude, selectedLongitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                var city = addresses[0].locality

                if (city != null){
                    selectedCity = city
                    var message = getString(R.string.text_city, selectedCity)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                } else {
                    selectedCity = getString(R.string.unknown_city)
                }
            }

        } catch (ex : Exception){
            Log.e("GEOCODER_ERROR", ex.message ?: "Error al obtener la ciudad al soltar el marcador")
            Toast.makeText(this, R.string.not_city, Toast.LENGTH_SHORT).show()
            VibratorUtil.vibrate(this, 1000)
        }
    }
}
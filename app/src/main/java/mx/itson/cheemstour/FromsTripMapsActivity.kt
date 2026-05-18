package mx.itson.cheemstour

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
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
    var latitudSeleccionada: Double = 0.0
    var longitudSeleccionada: Double = 0.0
    var ciudadSeleccionada: String = ""


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
    }

    override fun onClick(view: View?) {
        try {
            if (view?.id == R.id.btn_save_trip){

                var txfNameTrip = findViewById<EditText>(R.id.edit_text_name_trip)
                var nombreViaje = txfNameTrip.text.toString()

                if(nombreViaje.isNotEmpty() && latitudSeleccionada != 0.0){
                    Log.d("NUEVO_VIAJE", "Nombre: $nombreViaje")
                    Log.d("NUEVO_VIAJE", "Ciudad: $ciudadSeleccionada")
                    Log.d("NUEVO_VIAJE", "Latitud: $latitudSeleccionada")
                    Log.d("NUEVO_VIAJE", "Longitud: $longitudSeleccionada")

                    // Cargar los datos para un objeto:
                    var nuevoViaje = Trip()
                    nuevoViaje.name = nombreViaje
                    nuevoViaje.city = ciudadSeleccionada
                    nuevoViaje.latitude = latitudSeleccionada
                    nuevoViaje.longitude = longitudSeleccionada

                    val call: Call<ApiResponse> = RetrofitUtil.getApiCheems().saveTrip(nuevoViaje)
                    call.enqueue(object : retrofit2.Callback<ApiResponse> {

                        override fun onResponse(call: Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                            if (response.isSuccessful) {
                                // El código HTTP es 200
                                var respuestaApi = response.body()

                                if (respuestaApi != null && respuestaApi.success) {
                                    Toast.makeText(this@FromsTripMapsActivity, R.string.save_trip_toast, Toast.LENGTH_LONG).show()
                                    VibratorUtil.vibrate(this@FromsTripMapsActivity, 500)
                                    txfNameTrip.setText("") //limpiar el editext

                                } else {
                                    Toast.makeText(this@FromsTripMapsActivity, R.string.error_save_db, Toast.LENGTH_SHORT).show()
                                    VibratorUtil.vibrate(this@FromsTripMapsActivity, 1000)
                                }
                            } else {
                                // El servidor respondió codigo 500
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

            // Coordenadas iniciales predeterminadas
            var latLng = LatLng(27.9179, -110.9089)

            // Agregamos el marcador
            map!!.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.selected_trip))
                    .draggable(true)
            )

            map?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            map?.animateCamera(CameraUpdateFactory.zoomTo(10f))

            // Inicializamos
            latitudSeleccionada = latLng.latitude
            longitudSeleccionada = latLng.longitude

            // Le indicamos al mapa que esta misma clase maneje los eventos de arrastre
            map?.setOnMarkerDragListener(this)

        } catch (ex : Exception){
            Log.e("Error loading map", ex.message ?: "Ocurrió un error desconocido")
            Toast.makeText(this, R.string.err_loading_map, Toast.LENGTH_SHORT).show()
            VibratorUtil.vibrate(this, 800)
        }
    }

    // cuando levanta el marcador
    override fun onMarkerDragStart(marker: Marker) {
        Log.d("MAPA", "Inicia el arrastre del marcador")
        VibratorUtil.vibrate(this, 100)
    }

    // Mientras se mueve
    override fun onMarkerDrag(marker: Marker) {

    }

    // Cuando suelta el marcador
    override fun onMarkerDragEnd(marker: Marker) {
        try {
            // Extraemos la nueva posición del marcador
            var nuevaPosicion = marker.position
            latitudSeleccionada = nuevaPosicion.latitude
            longitudSeleccionada = nuevaPosicion.longitude

            Log.d("MAPA", "Marcador soltado en Lat: $latitudSeleccionada, Lng: $longitudSeleccionada")
            VibratorUtil.vibrate(this, 200)


            var geocoder = Geocoder(this, Locale.getDefault())
            var direcciones = geocoder.getFromLocation(latitudSeleccionada, longitudSeleccionada, 1)

            if (direcciones != null && direcciones.isNotEmpty()) {
                var ciudad = direcciones[0].locality

                if (ciudad != null){
                    ciudadSeleccionada = ciudad
                    var mensaje = getString(R.string.text_city, ciudadSeleccionada)
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                } else {
                    ciudadSeleccionada = getString(R.string.unknown_city)
                }
            }

        } catch (ex : Exception){
            Log.e("GEOCODER_ERROR", ex.message ?: "Error al obtener la ciudad al soltar el marcador")
            Toast.makeText(this, R.string.not_city, Toast.LENGTH_SHORT).show()
            VibratorUtil.vibrate(this, 1000)
        }
    }
}
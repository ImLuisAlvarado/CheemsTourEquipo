package mx.itson.cheemstour

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
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
import mx.itson.cheemstour.utils.RetrofitUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TripMapActivity : AppCompatActivity(), OnMapReadyCallback {

    var map : GoogleMap? = null

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
        getTrips()

    }
    fun getTrips() {
        val call: Call<List<Trip>> = RetrofitUtil.getApi().getTrips()
        call.enqueue(object : Callback<List<Trip>> {
            override fun onResponse(
                call: Call<List<Trip>>,
                response: Response<List<Trip>>) {
                val trips: List<Trip> = response.body()!!

                trips.forEach { t->
                    val latLng = LatLng(t.latitude, t.longitude)

                    map?.addMarker(
                        MarkerOptions().
                        position(latLng).
                        title(t.name).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.cheems))
                    )
                }
            }

            override fun onFailure(call: Call<List<Trip>>, t: Throwable) {
                Log.e("API_FAILURE", "Fallo la comunicación con la API: ${t.message}", t)
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap
            map!!.mapType = GoogleMap.MAP_TYPE_NORMAL

            getTrips()
        }catch (ex : Exception){
            Log.e( "Error loading map", ex.message ?: "Ocurrió un error desconocido")
        }
    }
}
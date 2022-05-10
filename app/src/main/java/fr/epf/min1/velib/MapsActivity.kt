package fr.epf.min1.velib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import fr.epf.min1.velib.api.LocalisationStation
import fr.epf.min1.velib.api.StationPosition
import fr.epf.min1.velib.databinding.ActivityMapsBinding
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var listStationPositions: List<StationPosition>

    private val bicycleIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.marker)
        BitmapHelper.vectorToBitmap(this, R.drawable.ic_pedal_bike, color)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.getMapAsync { googleMap ->
            addClusteredMarkers(googleMap)
            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                listStationPositions.forEach { bounds.include(LatLng(it.lat, it.lon)) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.station_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.list_station_action -> {
                startActivity(Intent(this, ListStationActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        val clusterManager = ClusterManager<StationPosition>(this, googleMap)
        clusterManager.renderer =
            StationRenderer(
                this,
                googleMap,
                clusterManager
            )

        // Add the places to the ClusterManager.
        clusterManager.addItems(listStationPositions)
        clusterManager.cluster()

        // Set ClusterManager as the OnCameraIdleListener so that it
        // can re-cluster when zooming in and out.
        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Paris.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
        val service = retrofit.create(LocalisationStation::class.java)

        runBlocking {
            listStationPositions = service.getStations().data.stations
            //mMap.setMinZoomPreference(12F)
            /*for (station in listStations){
                val coordinate = LatLng(station.lat, station.lon)
                mMap.addMarker(MarkerOptions().position(coordinate).title(station.name).icon(bicycleIcon))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
                mMap.setMinZoomPreference(12F)
            }*/
        }

    }
}
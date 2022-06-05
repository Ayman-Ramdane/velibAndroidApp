@file:Suppress("DEPRECATION")

package fr.epf.min1.velib

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.ClusterManager
import fr.epf.min1.velib.api.LocalisationStation
import fr.epf.min1.velib.api.StationDetails
import fr.epf.min1.velib.api.StationPosition
import fr.epf.min1.velib.api.VelibStationDetails
import fr.epf.min1.velib.database.FavoriteDatabase
import fr.epf.min1.velib.database.StationDatabase
import fr.epf.min1.velib.databinding.ActivityMapsBinding
import fr.epf.min1.velib.maps.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import fr.epf.min1.velib.maps.PermissionUtils.isPermissionGranted
import fr.epf.min1.velib.maps.PermissionUtils.requestPermission
import fr.epf.min1.velib.model.Favorite
import fr.epf.min1.velib.model.Station

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "MapsActivity"
lateinit var listStations: List<Station>
lateinit var listFavorite: List<Favorite>

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnRequestPermissionsResultCallback {
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var clusterManager: ClusterManager<Station>
    private lateinit var binding: ActivityMapsBinding

    //Location User
    private lateinit var locationButton: FloatingActionButton
    private lateinit var filterEbike: FloatingActionButton
    private lateinit var filterMechanical: FloatingActionButton
    private lateinit var filterDock: FloatingActionButton

    private var permissionDenied = false
    private lateinit var map: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    //Stations
    private var listStationPositions: List<StationPosition> = listOf()
    private var listStationDetails: List<StationDetails> = listOf()
    private var listStationsName: MutableList<String> = mutableListOf()
    var listStationMarkers: List<Station> = listOf()
    private lateinit var searchBar: AutoCompleteTextView
    private lateinit var searchAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            clusterManager = ClusterManager<Station>(this, googleMap)

            addClusteredMarkers(googleMap)
            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                listStationMarkers.forEach { bounds.include(LatLng(it.lat, it.lon)) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }

        }

        searchBar = findViewById(R.id.map_search_station)
        locationButton = findViewById(R.id.button_location_user)
        filterEbike = findViewById(R.id.map_filter_ebike)
        filterMechanical = findViewById(R.id.map_filter_mechanical)
        filterDock = findViewById(R.id.map_filter_docks)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.station_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.map_favorite_list_action -> {
                startActivity(Intent(this, ListFavoriteActivity::class.java))
            }

            R.id.map_synchro_api_list_action -> {
                if (checkForInternet(this)) {
                    refreshDataBase()
                } else {
                    Toast.makeText(this, getString(R.string.no_internet_access), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        clusterManager.renderer =
            StationRenderer(
                this,
                googleMap,
                clusterManager
            )

        Log.d(TAG, "addClusteredMarkers: $listStationMarkers")
        clusterManager.addItems(listStationMarkers)
        clusterManager.cluster()

        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }

        clusterManager.setOnClusterItemClickListener {
            val intent = Intent(this, DetailsStationActivity::class.java)
            intent.putExtra("station_id", it.station_id)
            intent.putExtra("station_name", it.name)
            startActivity(intent)
            true
        }
    }

    private fun refreshMarkers(googleMap: GoogleMap) {
        clusterManager.clearItems()
        clusterManager.cluster()
        setupSearchBarAdapter()
        addClusteredMarkers(googleMap)
    }

    private fun setupSearchBarAdapter() {
        listStationsName = mutableListOf()
        listStationMarkers.forEach { listStationsName.add(it.name) }

        searchAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listStationsName)
        searchBar.setAdapter(searchAdapter)
    }

    private fun synchroApiStationLocalisation() {
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
        }
    }

    private fun synchroApiStationDetails() {
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

        val service = retrofit.create(VelibStationDetails::class.java)

        runBlocking {
            val result = service.getStations()
            listStationDetails = result.data.stations
        }
    }

    private fun refreshDataBase() {
        val dbStation = StationDatabase.createDatabase(this)
        val stationDao = dbStation.stationDao()

        synchroApiStationLocalisation()
        synchroApiStationDetails()

        runBlocking {
            stationDao.deleteAll()
        }

        listStationPositions.zip(listStationDetails).map {
            Station(
                it.first.station_id,
                it.first.name,
                it.first.lat,
                it.first.lon,
                it.first.stationCode,
                it.second.is_installed,
                it.second.is_renting,
                it.second.is_returning,
                it.second.numBikesAvailable,
                it.second.numDocksAvailable,
                it.second.num_bikes_available_types?.get(0)?.get("mechanical"),
                it.second.num_bikes_available_types?.get(1)?.get("ebike"),
                !it.first.rental_methods.isNullOrEmpty(),
                it.second.last_reported
            )
        }.map {
            runBlocking {
                stationDao.insert(it)
            }
        }

        runBlocking {
            listStations = stationDao.getAll()
        }
        listStations = listStations.filter { it.is_installed == 1 }

        listStationMarkers = listStations

        dbStation.close()
    }

    @SuppressLint("MissingPermission")
    private fun checkForInternet(context: Context): Boolean {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false

        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun refreshListStationFromDataBase() {
        val dbStation = StationDatabase.createDatabase(this)
        val stationDao = dbStation.stationDao()

        runBlocking {
            listStations = stationDao.getAll()
        }
        listStations = listStations.filter { it.is_installed == 1 }

        listStationMarkers = listStations

        dbStation.close()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        enableMyLocation()
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        locationButton.setOnClickListener {
            if (map.myLocation != null) {
                val userLocationLat = map.myLocation.latitude
                val userLocationLon = map.myLocation.longitude
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            userLocationLat,
                            userLocationLon
                        ), 20F
                    )
                )
            } else {
                Toast.makeText(this, R.string.no_location_found, Toast.LENGTH_SHORT).show()
            }
        }

        if (checkForInternet(this)) {
            refreshDataBase()
        } else {
            refreshListStationFromDataBase()
        }

        setupSearchBarAdapter()
        searchBar.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedItemText = parent.getItemAtPosition(position)
                val station =
                    listStations.filter { station -> station.name == selectedItemText }[0]

                val bounds = LatLngBounds.builder()
                bounds.include(LatLng(station.lat, station.lon))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }

        val dbFavorite = FavoriteDatabase.createDatabase(this)
        val favoriteDao = dbFavorite.favoriteDao()

        runBlocking {
            listFavorite = favoriteDao.getAll()
        }

        dbFavorite.close()

        filterEbike.setOnClickListener {
            if (!filterEbike.isExpanded) {
                if (filterDock.isExpanded || filterMechanical.isExpanded) {
                    refreshListStationFromDataBase()
                    filterDock.isExpanded = false
                    filterDock.size = FloatingActionButton.SIZE_NORMAL

                    filterMechanical.isExpanded = false
                    filterMechanical.size = FloatingActionButton.SIZE_NORMAL
                }

                listStationMarkers =
                    listStationMarkers.filter { it.num_ebikes_available!! > 0 && it.is_renting == 1 }

                refreshMarkers(googleMap)

                filterEbike.isExpanded = !filterEbike.isExpanded
                filterEbike.size = FloatingActionButton.SIZE_MINI
            } else {
                refreshListStationFromDataBase()
                refreshMarkers(googleMap)
                filterEbike.isExpanded = !filterEbike.isExpanded
                filterEbike.size = FloatingActionButton.SIZE_NORMAL
            }
        }

        filterMechanical.setOnClickListener {
            if (!filterMechanical.isExpanded) {
                if (filterDock.isExpanded || filterEbike.isExpanded) {
                    refreshListStationFromDataBase()
                    filterDock.isExpanded = false
                    filterDock.size = FloatingActionButton.SIZE_NORMAL

                    filterEbike.isExpanded = false
                    filterEbike.size = FloatingActionButton.SIZE_NORMAL
                }

                listStationMarkers =
                    listStationMarkers.filter { it.num_Mechanical_bikes_available!! > 0 && it.is_renting == 1 }

                refreshMarkers(googleMap)

                filterMechanical.isExpanded = !filterMechanical.isExpanded
                filterMechanical.size = FloatingActionButton.SIZE_MINI
            } else {
                refreshListStationFromDataBase()
                refreshMarkers(googleMap)
                filterMechanical.isExpanded = !filterMechanical.isExpanded
                filterMechanical.size = FloatingActionButton.SIZE_NORMAL
            }
        }

        filterDock.setOnClickListener {
            if (!filterDock.isExpanded) {
                if (filterEbike.isExpanded || filterMechanical.isExpanded) {
                    refreshListStationFromDataBase()
                    filterEbike.isExpanded = false
                    filterEbike.size = FloatingActionButton.SIZE_NORMAL

                    filterMechanical.isExpanded = false
                    filterMechanical.size = FloatingActionButton.SIZE_NORMAL
                }

                listStationMarkers =
                    listStationMarkers.filter { it.numDocksAvailable!! > 0 && it.is_returning == 1 }

                refreshMarkers(googleMap)

                filterDock.isExpanded = !filterDock.isExpanded
                filterDock.size = FloatingActionButton.SIZE_MINI
            } else {
                refreshListStationFromDataBase()
                refreshMarkers(googleMap)
                filterDock.isExpanded = !filterDock.isExpanded
                filterDock.size = FloatingActionButton.SIZE_NORMAL
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            enableMyLocation()
        } else {
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }
}
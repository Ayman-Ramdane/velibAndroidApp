package fr.epf.min1.velib

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import fr.epf.min1.velib.api.LocalisationStation
import fr.epf.min1.velib.api.StationDetails
import fr.epf.min1.velib.api.StationPosition
import fr.epf.min1.velib.api.VelibStationDetails
import fr.epf.min1.velib.database.FavoriteDatabase
import fr.epf.min1.velib.database.StationDatabase
import fr.epf.min1.velib.databinding.ActivityMapsBinding
import fr.epf.min1.velib.maps.*
import fr.epf.min1.velib.model.LocationUser
import fr.epf.min1.velib.model.Favorite
import fr.epf.min1.velib.model.Station

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "MapsActivity"
private lateinit var listStationPositions: List<StationPosition>
private lateinit var listStationDetails: List<StationDetails>
lateinit var listStations: List<Station>
lateinit var listFavorite: List<Favorite>


//Location User
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private lateinit var map: GoogleMap
var permissionApproved: Boolean = false
private var locationUserLast = LocationUser(0.0, 0.0)
private var locationUserNew = LocationUser(0.0, 0.0)
private lateinit var markerLocationUser: Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityMapsBinding

    //Location User
    private var foregroundOnlyLocationServiceBound = false
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationButton: Button

    //Stations names
    private var listStationsName: MutableList<String> = mutableListOf()

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
        mapFragment.getMapAsync { googleMap ->
            addClusteredMarkers(googleMap)
            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                listStations.forEach { bounds.include(LatLng(it.lat, it.lon)) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }
            map = googleMap

            listStations.forEach { listStationsName.add(it.name) }

            val searchBar = findViewById<AutoCompleteTextView>(R.id.map_search_station)
            val searchAdapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, listStationsName)
            searchBar.setAdapter(searchAdapter)

            searchBar.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val selectedItemText = parent.getItemAtPosition(position)
                    val station =
                        listStations.filter { station -> station.name == selectedItemText }[0]

                    val bounds = LatLngBounds.builder()
                    bounds.include(LatLng(station.lat, station.lon))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
                }
        }

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        locationButton = findViewById(R.id.button_location_user)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.station_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.map_station_list_action -> {
                startActivity(Intent(this, ListStationActivity::class.java))
            }

            R.id.map_favorite_list_action -> {
                startActivity(Intent(this, ListFavoriteActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        val clusterManager = ClusterManager<Station>(this, googleMap)
        clusterManager.renderer =
            StationRenderer(
                this,
                googleMap,
                clusterManager
            )

        clusterManager.addItems(listStations)
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

    @SuppressLint("MissingPermission")
    private fun checkForInternet(context: Context): Boolean {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false

            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    override fun onStart() {
        super.onStart()

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {}

    private fun locationPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestLocationPermissions() {
        val provideRationale = locationPermissionApproved()

        if (provideRationale) {
            Snackbar.make(
                findViewById(R.id.activity_maps),
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MapsActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@MapsActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    // Permission denied.
                    //updateButtonState(false)

                    Snackbar.make(
                        findViewById(R.id.activity_maps),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        //Location User
        map = googleMap
        permissionApproved = locationPermissionApproved()

        locationButton.setOnClickListener {
            if (locationPermissionApproved()) {
                foregroundOnlyLocationService?.subscribeToLocationUpdates()
                    ?: Log.d(TAG, "Service Not Bound")
            } else {
                requestLocationPermissions()
            }

            if (locationUserNew.latitude != 0.0 && locationUserNew.longitude != 0.0) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            locationUserNew.latitude,
                            locationUserNew.longitude
                        )
                    )
                )
            }
        }

        val dbStation = StationDatabase.createDatabase(this)

        val stationDao = dbStation.stationDao()

        if (checkForInternet(this)) {
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
                    it.second.numDocksAvailable
                )
            }.map {
                runBlocking {
                    stationDao.insert(it)
                }
            }

            runBlocking {
                listStations = stationDao.getAll()
            }
        } else {
            runBlocking {
                listStations = stationDao.getAll()
            }
        }
        dbStation.close()

        val dbFavorite = FavoriteDatabase.createDatabase(this)

        val favoriteDao = dbFavorite.favoriteDao()

        runBlocking {
            listFavorite = favoriteDao.getAll()
        }

        dbFavorite.close()
    }
}

fun locationUserOnMap(locationUser: LocationUser) {
    locationUserNew = locationUser

    if (locationUserLast.latitude == 0.0 && locationUserLast.longitude == 0.0) {
        val coordinateNew = LatLng(locationUser.latitude, locationUser.longitude)
        markerLocationUser = map.addMarker(MarkerOptions().position(coordinateNew))!!

        locationUserLast = locationUser
    } else {
        markerLocationUser.remove()

        val coordinateNew = LatLng(locationUser.latitude, locationUser.longitude)
        markerLocationUser = map.addMarker(MarkerOptions().position(coordinateNew))!!

        locationUserLast = locationUser

    }
}
package fr.epf.min1.velib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import fr.epf.min1.velib.api.VelibStationDetails
import fr.epf.min1.velib.model.Station
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "DetailsStationActivity"

class DetailsStationActivity : AppCompatActivity() {

    private val stations: MutableList<Station> = mutableListOf()
    var stationDetails: Station = Station(0.0, null, null, null, null, null, )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_station)

        val stationId = intent.getDoubleExtra("station_id", 0.0)
        val stationName = intent.getStringExtra("station_name")

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
            var listStopDetails = service.getStations().data.stations
            listStopDetails.map {
                val (station_id, is_installed, is_renting, is_returning, numBikesAvailable, numDocksAvailable) = it

                Station(
                    station_id,
                    is_installed,
                    is_renting,
                    is_returning,
                    numBikesAvailable,
                    numDocksAvailable
                )
            }
                .map {
                    stations.add(it)
                }
        }

        val stationDetails = stations.filter { station -> station.station_id.equals(stationId) }[0]

        /*for (station in stations) {
            if (station.station_id.equals(stationId.toString())) {
                stationDetails = Station(
                    station.station_id,
                    station.is_installed,
                    station.is_renting,
                    station.is_returning,
                    station.numBikesAvailable,
                    station.numDocksAvailable
                )
                val stationId1 = station.station_id
                Log.d(TAG, "onCreate: $stationId1")
            }
        }*/

        val nameTextView = findViewById<TextView>(R.id.details_stations_nom_textview)
        nameTextView.text = stationName

        val numBikes = stationDetails.numBikesAvailable
        val numBikesTextView = findViewById<TextView>(R.id.details_stations_velos_textview)
        numBikesTextView.text = numBikes.toString()

        val numDocks = stationDetails.numDocksAvailable
        val numDocksTextView = findViewById<TextView>(R.id.details_stations_bornes_textview)
        numDocksTextView.text = numDocks.toString()
    }
}
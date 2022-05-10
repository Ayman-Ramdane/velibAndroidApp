package fr.epf.min1.velib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.epf.min1.velib.api.LocalisationStation
import fr.epf.min1.velib.api.StationPosition
import fr.epf.min1.velib.api.VelibStationDetails
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "ListStationActivity"

class ListStationActivity : AppCompatActivity() {

    private lateinit var listStationPositions: List<StationPosition>
    private var stationAdapter: StationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
/*            var listStopDetails = service.getStations().data.stations
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
                }*/
        }

        //SynchroApi()

        setContentView(R.layout.activity_list_station)

        val recyclerView = findViewById<RecyclerView>(R.id.list_stations_recyclerview)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        stationAdapter = StationAdapter(listStationPositions)
        recyclerView.adapter = stationAdapter
    }

    private fun SynchroApi() {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val station = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(station)
            .build()

        val service = retrofit.create(VelibStationDetails::class.java)

        runBlocking {
            val result = service.getStations()
            //Log.d(TAG, "synchroAPI: ${result}")
            val results = result.data.stations
            Log.d(TAG, "SynchroApi results: $results")

            for (stop in results) {
                Log.d(TAG, "SynchroApi Stop: $stop")
            }

/*            results.map {
                val (station_id, is_installed, is_renting, is_returning, numBikesAvailable, numDocksAvailable, num_bikes_available_types) = it
                Station(
                    station_id,
                    is_installed,
                    is_renting,
                    is_returning,
                    numBikesAvailable,
                    numDocksAvailable,
                    num_bikes_available_types
                )
            }
                .map {
                    stations.add(it)
                    //Log.d(TAG, "SynchroApi: $it")
                }*/
        }
    }
}
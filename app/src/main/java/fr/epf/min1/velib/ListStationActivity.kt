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

        setContentView(R.layout.activity_list_station)

        val recyclerView = findViewById<RecyclerView>(R.id.list_stations_recyclerview)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        listStationPositions = fr.epf.min1.velib.listStationPositions
        stationAdapter = StationAdapter(listStationPositions)
        recyclerView.adapter = stationAdapter
    }
}
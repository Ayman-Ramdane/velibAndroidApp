package fr.epf.min1.velib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

private const val TAG = "DetailsStationActivity"

class DetailsStationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_station)

        val stationId = intent.getDoubleExtra("station_id", 0.0)
        val stationName = intent.getStringExtra("station_name")

        val liststations = stations
        val stationDetails = liststations.filter { station -> station.station_id.equals(stationId) }[0]

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

        val nameTextView = findViewById<TextView>(R.id.details_stations_name_textview)
        nameTextView.text = stationName

        val numBikes = stationDetails.numBikesAvailable
        val numBikesTextView = findViewById<TextView>(R.id.details_stations_bikes_textview)
        numBikesTextView.text = numBikes.toString()

        val numDocks = stationDetails.numDocksAvailable
        val numDocksTextView = findViewById<TextView>(R.id.details_stations_docks_textview)
        numDocksTextView.text = numDocks.toString()
    }
}
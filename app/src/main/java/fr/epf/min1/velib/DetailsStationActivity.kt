package fr.epf.min1.velib

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import fr.epf.min1.velib.database.FavoriteDatabase
import fr.epf.min1.velib.model.Favorite
import fr.epf.min1.velib.model.Station
import kotlinx.coroutines.runBlocking
import java.sql.Date
import java.text.SimpleDateFormat

class DetailsStationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_station)

        val stationId = intent.getLongExtra("station_id", 0)

        val stationDetails = listStations.find { it.station_id == stationId } ?: Station(
            0,
            "Station introuvable",
            0.0,
            0.0,
            "0",
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            false,
            0
        )

        val nameTextView = findViewById<TextView>(R.id.details_stations_name_textview)
        nameTextView.text = stationDetails.name

        val numBikes = stationDetails.numBikesAvailable
        val numBikesTextView = findViewById<TextView>(R.id.details_stations_bikes_textview)
        numBikesTextView.text = numBikes.toString()

        val numDocks = stationDetails.numDocksAvailable
        val numDocksTextView = findViewById<TextView>(R.id.details_stations_docks_textview)
        numDocksTextView.text = numDocks.toString()

        val numMechanicalBikesAvailable = stationDetails.num_Mechanical_bikes_available
        val numMechanicalBikesAvailableTextView =
            findViewById<TextView>(R.id.details_stations_nb_meca_textview)
        numMechanicalBikesAvailableTextView.text = numMechanicalBikesAvailable.toString()

        val numEbikesAvailable = stationDetails.num_ebikes_available
        val numEbikesAvailableTextView =
            findViewById<TextView>(R.id.details_stations_nb_ebike_textview)
        numEbikesAvailableTextView.text = numEbikesAvailable.toString()

        val creditCardAvailable = stationDetails.credit_card_available
        val creditCardAvailableTextView =
            findViewById<TextView>(R.id.details_stations_credit_card_available)

        val creditCardAvailableImageView = findViewById<ImageView>(R.id.credit_card_image)
        creditCardAvailableTextView.isVisible = creditCardAvailable
        creditCardAvailableImageView.isVisible = creditCardAvailable

        val lastReportedUpdate = stationDetails.last_reported
        val lastReportedUpdateTextView = findViewById<TextView>(R.id.last_update_data_bike)
        val lastReportedUpdateDate = Date(lastReportedUpdate * 1000)
        val formatter = SimpleDateFormat.getDateTimeInstance()
        val formatedDate = formatter.format(lastReportedUpdateDate)
        lastReportedUpdateTextView.text = formatedDate.toString()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.station_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        val menuItem = menu.findItem(R.id.details_station_favorite_button)

        val iconFavorite = getDrawable(R.drawable.ic_baseline_favorite_24)
        val iconNotFavorite = getDrawable(R.drawable.ic_baseline_favorite_border_24)

        val stationId = intent.getLongExtra("station_id", 0)
        val isEmpty = listFavorite.none { favorite -> favorite.favorite_station_id == stationId }
        menuItem.isChecked = !isEmpty

        if (isEmpty) menuItem.icon = iconNotFavorite else menuItem.icon = iconFavorite

        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.details_station_favorite_button -> {
                val iconFavorite = getDrawable(R.drawable.ic_baseline_favorite_24)
                val iconNotFavorite = getDrawable(R.drawable.ic_baseline_favorite_border_24)

                val stationId = intent.getLongExtra("station_id", 0)
                val favorite = Favorite(stationId)

                val dbFavorite = FavoriteDatabase.createDatabase(this)

                val favoriteDao = dbFavorite.favoriteDao()

                val checked = item.isChecked
                if (checked) {
                    runBlocking {
                        favoriteDao.delete(favorite)
                    }

                    item.isChecked = false

                    item.icon = iconNotFavorite
                } else {
                    runBlocking {
                        favoriteDao.addFavorite(favorite)
                    }

                    item.isChecked = true

                    item.icon = iconFavorite
                }

                runBlocking {
                    listFavorite = favoriteDao.getAll()
                }
                dbFavorite.close()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
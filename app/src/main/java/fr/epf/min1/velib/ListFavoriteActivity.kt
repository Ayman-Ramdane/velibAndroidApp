package fr.epf.min1.velib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.epf.min1.velib.model.Station

private const val TAG = "ListFavoriteActivity"

class ListFavoriteActivity : AppCompatActivity() {

    private var favoriteAdapter: FavoriteAdapter? = null

    val listFavoriteStations : MutableList<Station> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listFavorite.forEach {  favorite ->
            val favoriteId = favorite.favorite_station_id
            listStations.forEach {
                if (it.station_id == favoriteId) listFavoriteStations.add(it)
            }
        }

        setContentView(R.layout.activity_list_favorite)

        val recyclerView = findViewById<RecyclerView>(R.id.list_favorite_recyclerview)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        favoriteAdapter = FavoriteAdapter(listFavoriteStations)
        recyclerView.adapter = favoriteAdapter
    }
}
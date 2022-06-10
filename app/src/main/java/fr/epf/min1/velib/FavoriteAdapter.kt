package fr.epf.min1.velib

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.epf.min1.velib.model.Station

class FavoriteAdapter(private val favoriteStations: List<Station>) :
    RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val stationView = inflater.inflate(R.layout.adapter_favorite, parent, false)
        return FavoriteViewHolder(stationView)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favoriteStation = favoriteStations[position]

        holder.view.setOnClickListener {
            val context = it.context
            val intent = Intent(context, DetailsStationActivity::class.java)
            intent.putExtra("station_id", favoriteStation.station_id)
            context.startActivity(intent)
        }

        val stationNameTextView = holder.view.findViewById<TextView>(R.id.adapter_favorite_title)
        val stationBikeTextView =
            holder.view.findViewById<TextView>(R.id.adapter_favorite_bike_number)
        val stationDockTextView =
            holder.view.findViewById<TextView>(R.id.adapter_favorite_dock_number)
        stationNameTextView.text = favoriteStation.name
        stationBikeTextView.text = favoriteStation.numBikesAvailable.toString()
        stationDockTextView.text = favoriteStation.numDocksAvailable.toString()
    }

    override fun getItemCount() = favoriteStations.size
}
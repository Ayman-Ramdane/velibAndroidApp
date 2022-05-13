package fr.epf.min1.velib

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.epf.min1.velib.api.StationPosition
import fr.epf.min1.velib.model.Station

class StationAdapter(private val stations: List<Station>) :
    RecyclerView.Adapter<StationAdapter.StationViewHolder>() {

    class StationViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val stationView = inflater.inflate(R.layout.adapter_station, parent, false)
        return StationViewHolder(stationView)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stations[position]

        holder.view.setOnClickListener {
            val context = it.context
            val intent = Intent(context, DetailsStationActivity::class.java)
            intent.putExtra("station_id", station.station_id)
            intent.putExtra("station_name", station.name)
            context.startActivity(intent)
        }

        val clientTextView = holder.view.findViewById<TextView>(R.id.adapter_station_text)
        clientTextView.text = "${station.name}"
    }

    override fun getItemCount() = stations.size
}
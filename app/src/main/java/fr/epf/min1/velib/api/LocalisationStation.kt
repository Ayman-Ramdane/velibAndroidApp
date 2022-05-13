package fr.epf.min1.velib.api

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import retrofit2.http.GET

interface LocalisationStation {
    @GET("station_information.json")
    suspend fun getStations() : GetStationResult

}

data class GetStationResult(val data: DataStation)
data class DataStation(val stations: List<StationPosition>)
data class StationPosition(val station_id: Double, val name: String, val lat: Double, val lon: Double, val stationCode: String) :
    ClusterItem {
    override fun getPosition(): LatLng = LatLng(lat, lon)

    override fun getTitle(): String = name

    override fun getSnippet(): String = stationCode
}

package fr.epf.min1.velib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

@Entity
data class Station(
    @PrimaryKey @ColumnInfo val station_id: Long,
    @ColumnInfo val name: String,
    @ColumnInfo val lat: Double,
    @ColumnInfo val lon: Double,
    @ColumnInfo val stationCode: String,
    @ColumnInfo val is_installed: Int?,
    @ColumnInfo val is_renting: Int?,
    @ColumnInfo val is_returning: Int?,
    @ColumnInfo val numBikesAvailable: Int?,
    @ColumnInfo val numDocksAvailable: Int?,
    //val num_bikes_available_types: List<Map<String, Int>>?
) :
    ClusterItem {
    override fun getPosition(): LatLng = LatLng(lat, lon)

    override fun getTitle(): String = name

    override fun getSnippet(): String = stationCode
}
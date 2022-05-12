package fr.epf.min1.velib.api

import retrofit2.http.GET

interface VelibStationDetails {

    @GET("station_status.json")
    suspend fun getStations() : GetStationDetails
}

data class GetStationDetails(val data: DataStationDetails)
data class DataStationDetails(val stations: List<StationDetails>)
data class StationDetails (
    val station_id: Double,
    val is_installed: Int?,
    val is_renting: Int?,
    val is_returning: Int?,
    val numBikesAvailable: Int?,
    val numDocksAvailable: Int?,
    val num_bikes_available_types: List<AvaibleByType>?
)

data class AvaibleByType (val mechanical: Int?, val ebike: Int?) //pas sûr que ça soit correct
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
    val is_installed: Double?,
    val is_renting: Double?,
    val is_returning: Double?,
    val numBikesAvailable: Double?,
    val numDocksAvailable: Double?,
    //val num_bikes_available_types: Int?
)
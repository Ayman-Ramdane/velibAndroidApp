package fr.epf.min1.velib.api

import retrofit2.http.GET
import retrofit2.http.Query

interface LocalisationStation {
    @GET("station_information.json")
    suspend fun getStations() : GetStationResult

}

data class GetStationResult(val data: DataStation)
data class DataStation(val stations: List<Station>)
data class Station(val station_id: Double, val name: String, val lat: Double, val lon: Double)

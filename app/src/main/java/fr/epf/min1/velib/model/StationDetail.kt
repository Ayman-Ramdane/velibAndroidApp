package fr.epf.min1.velib.model


data class StationDetail(
    val station_id: Double,
    val is_installed: Double?,
    val is_renting: Double?,
    val is_returning: Double?,
    val numBikesAvailable: Double?,
    val numDocksAvailable: Double?,
    //val num_bikes_available_types: Int?,
)
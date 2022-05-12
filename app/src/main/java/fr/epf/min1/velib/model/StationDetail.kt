package fr.epf.min1.velib.model


data class StationDetail(
    val station_id: Double,
    val is_installed: Int?,
    val is_renting: Int?,
    val is_returning: Int?,
    val numBikesAvailable: Int?,
    val numDocksAvailable: Int?,
    //val num_bikes_available_types: Int?,
)
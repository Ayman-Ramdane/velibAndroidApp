package fr.epf.min1.velib.dao

import androidx.room.*
import fr.epf.min1.velib.model.Station
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDAO {
    @Query("SELECT * FROM Station ORDER BY name ASC")
    suspend fun getAll(): List<Station>

    @Query("DELETE FROM Station")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(station: Station)
//
//    @Delete
//    fun delete(station: Station)
//
//    @Update
//    fun update(vararg stations: List<Station>)
}
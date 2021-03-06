package fr.epf.min1.velib.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.epf.min1.velib.model.Station

@Dao
interface StationDAO {
    @Query("SELECT * FROM Station ORDER BY name ASC")
    suspend fun getAll(): List<Station>

    @Query("DELETE FROM Station")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(station: Station)
}
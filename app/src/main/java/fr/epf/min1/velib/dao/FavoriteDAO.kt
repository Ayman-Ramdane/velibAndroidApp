package fr.epf.min1.velib.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.epf.min1.velib.model.Favorite

@Dao
interface FavoriteDAO {
    @Query("SELECT * FROM Favorite")
    suspend fun getAll(): List<Favorite>

    @Query("DELETE FROM Favorite")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(favorite: Favorite)

    @Insert
    suspend fun addFavorite(favorite: Favorite)
}
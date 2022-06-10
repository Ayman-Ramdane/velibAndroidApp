package fr.epf.min1.velib.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.epf.min1.velib.dao.FavoriteDAO
import fr.epf.min1.velib.dao.StationDAO
import fr.epf.min1.velib.model.Favorite
import fr.epf.min1.velib.model.Station

@Database(entities = [Station::class], version = 6)
abstract class StationDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDAO

    companion object {
        fun createDatabase(context: Context): StationDatabase {
            return Room.databaseBuilder(
                context,
                StationDatabase::class.java,
                "Station"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

@Database(entities = [Favorite::class], version = 1)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDAO

    companion object {
        fun createDatabase(context: Context): FavoriteDatabase {
            return Room.databaseBuilder(
                context,
                FavoriteDatabase::class.java,
                "Favorite"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
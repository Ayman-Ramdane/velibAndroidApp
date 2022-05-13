package fr.epf.min1.velib.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.epf.min1.velib.dao.StationDAO
import fr.epf.min1.velib.model.Station


@Database(entities = [Station::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDAO

    companion object {
        fun createDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "Station"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

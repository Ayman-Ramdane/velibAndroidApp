package fr.epf.min1.velib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Favorite(
    @PrimaryKey @ColumnInfo val favorite_station_id: Long
)
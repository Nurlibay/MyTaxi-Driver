package uz.nurlibaydev.mytaxitesttask.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import uz.nurlibaydev.mytaxitesttask.utils.Constants.LOCATION_TABLE

/**
 *  Created by Nurlibay Koshkinbaev on 11/03/2023 18:40
 */

@Parcelize
@Entity(tableName = LOCATION_TABLE)
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "latitude") val lat: Double,
    @ColumnInfo(name = "longitude") val lng: Double,
    val time: String,
    val bearing: Float
) : Parcelable

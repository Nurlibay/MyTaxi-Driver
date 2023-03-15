package uz.nurlibaydev.mytaxitesttask.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 *  Created by Nurlibay Koshkinbaev on 11/03/2023 18:40
 */

@Parcelize
@Entity(tableName = "location_table")
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "latitude") val lat: Double,
    @ColumnInfo(name = "longitude") val lng: Double,
    val time: String,
    val bearing: Float
) : Parcelable

package uz.nurlibaydev.mytaxitesttask.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.models.Bearing
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 *  Created by Nurlibay Koshkinbaev on 11/03/2023 18:40
 */

@Parcelize
@Entity(tableName = "location_table")
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val lat: Double,
    val lng: Double,
    val time: String,
    val bearing: Float
) : Parcelable
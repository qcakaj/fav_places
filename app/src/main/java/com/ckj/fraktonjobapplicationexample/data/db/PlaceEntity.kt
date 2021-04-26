package com.ckj.fraktonjobapplicationexample.data.db

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ckj.fraktonjobapplicationexample.util.randomColor

@Entity(tableName = "places")
data class PlaceEntity(
    @ColumnInfo(name = "name")
    val name: String? = "Current Location",
    @ColumnInfo(name = "latitude")
    val latitude: Double? = 0.0,
    @ColumnInfo(name = "longitude")
    val longitude: Double? = 0.0,
    @ColumnInfo(name = "imageUri")
    val imageUri: String? = "",
    @ColumnInfo(name = "isCurrent")
    var isCurrentLocation: Boolean = false,
    @ColumnInfo(name = "placeHolderColor")
    @ColorInt var placeHolderColor: Int = randomColor()

) {
    @PrimaryKey(autoGenerate = true)
    var placeId: Int? = null
}
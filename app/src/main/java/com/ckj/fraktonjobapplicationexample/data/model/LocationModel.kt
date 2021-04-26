package com.ckj.fraktonjobapplicationexample.data.model

import android.location.Location

data class LocationModel(val longitude: Double,val latitude:Double)

fun Location?.mapToLocationModel() = this?.longitude?.let {
    LocationModel(
        longitude = it,
        latitude=this.latitude
    )
}

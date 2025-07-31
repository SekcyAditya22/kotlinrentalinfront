package com.example.rentalinn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationData(
    val address: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable

data class LocationPickerResult(
    val pickupLocation: LocationData?,
    val returnLocation: LocationData?
)

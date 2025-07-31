package com.example.rentalinn.model

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.JsonToken
import org.json.JSONArray
import java.io.IOException

// Custom TypeAdapter for converting between string representation of JSON arrays and List<String>
class StringArrayAdapter : TypeAdapter<List<String>>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: List<String>?) {
        if (value == null) {
            out.nullValue()
            return
        }
        val jsonArray = JSONArray()
        value.forEach { jsonArray.put(it) }
        out.value(jsonArray.toString())
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): List<String>? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        
        // Handle both string representation of arrays and actual JSON arrays
        return when (reader.peek()) {
            JsonToken.STRING -> {
                // Handle string representation of array: "[\"/path/to/file.jpg\"]"
                val jsonStr = reader.nextString()
                try {
                    // Handle empty string or "null" string
                    if (jsonStr.isNullOrBlank() || jsonStr == "null") {
                        return emptyList()
                    }
                    
                    // Handle string that's not a JSON array but a single value
                    if (!jsonStr.startsWith("[") && !jsonStr.endsWith("]")) {
                        return listOf(jsonStr)
                    }
                    
                    val jsonArray = JSONArray(jsonStr)
                    val result = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getString(i)
                        if (item.isNotBlank() && item != "null") {
                            result.add(item)
                        }
                    }
                    result
                } catch (e: Exception) {
                    // If parsing fails, return the original string as a single item list
                    // unless it's empty or "null"
                    if (jsonStr.isNotBlank() && jsonStr != "null") {
                        listOf(jsonStr)
                    } else {
                        emptyList()
                    }
                }
            }
            JsonToken.BEGIN_ARRAY -> {
                // Handle actual JSON array: ["/path/to/file.jpg"]
                reader.beginArray()
                val result = mutableListOf<String>()
                while (reader.hasNext()) {
                    val item = reader.nextString()
                    if (item.isNotBlank() && item != "null") {
                        result.add(item)
                    }
                }
                reader.endArray()
                result
            }
            else -> {
                // Skip unknown tokens
                reader.skipValue()
                emptyList()
            }
        }
    }
}

data class Vehicle(
    val id: Int,
    val title: String,
    val brand: String,
    val model: String,
    @SerializedName("vehicle_category")
    val vehicleCategory: String,
    val year: Int,
    @SerializedName("price_per_day")
    val pricePerDay: Double,
    val description: String? = null,
    val status: String = "available",
    @JsonAdapter(StringArrayAdapter::class)
    val photos: List<String>? = null,
    @JsonAdapter(StringArrayAdapter::class)
    val features: List<String>? = null,
    val transmission: String,
    @SerializedName("fuel_type")
    val fuelType: String,
    @SerializedName("passenger_capacity")
    val passengerCapacity: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    // New fields for availability tracking
    @SerializedName("available_units")
    val availableUnits: Int? = null,
    @SerializedName("is_available")
    val isAvailableForBooking: Boolean? = null,
    @SerializedName("overlapping_rentals")
    val overlappingRentals: Int? = null,
    val units: List<VehicleUnit>? = null
) {
    companion object {
        private const val BASE_URL = "https://beexpress.peachy.icu"
    }
    
    fun getFormattedPrice(): String = "Rp ${pricePerDay.toInt()}/day"
    
    fun getMainPhoto(): String? {
        val photoPath = photos?.firstOrNull()
        return if (photoPath != null) {
            // Check if the path already has the base URL
            if (photoPath.startsWith("http")) {
                photoPath
            } else {
                // Prepend the base URL to the relative path
                "$BASE_URL$photoPath"
            }
        } else {
            null
        }
    }
    
    fun isAvailable(): Boolean {
        return units?.any { it.isAvailable() } ?: (availableUnits?.let { it > 0 } ?: (status == "available"))
    }

    fun getAvailableUnits(): Int {
        return units?.count { it.isAvailable() } ?: (availableUnits ?: 0)
    }

    fun getAvailabilityText(): String {
        val available = getAvailableUnits()
        return when {
            available <= 0 -> "Not Available"
            available == 1 -> "1 unit available"
            else -> "$available units available"
        }
    }

    fun getStatusColor(): androidx.compose.ui.graphics.Color {
        return when {
            !isAvailable() -> androidx.compose.ui.graphics.Color(0xFFEF4444) // Red
            getAvailableUnits() <= 2 -> androidx.compose.ui.graphics.Color(0xFFF59E0B) // Orange
            else -> androidx.compose.ui.graphics.Color(0xFF10B981) // Green
        }
    }

    // New methods for vehicle units support
    fun getAvailableUnitsFromList(): Int {
        return units?.count { it.isAvailable() } ?: getAvailableUnits()
    }

    fun getTotalUnitsCount(): Int {
        return units?.size ?: 0
    }

    fun getRentedUnitsCount(): Int {
        return units?.count { it.isRented() } ?: 0
    }

    fun getMaintenanceUnitsCount(): Int {
        return units?.count { it.isInMaintenance() } ?: 0
    }

    fun getOutOfServiceUnitsCount(): Int {
        return units?.count { it.isOutOfService() } ?: 0
    }

    fun getAvailableUnitsList(): List<VehicleUnit> {
        return units?.filter { it.isAvailable() } ?: emptyList()
    }

    fun getVehicleStatus(): String {
        val availableCount = getAvailableUnitsFromList()
        val totalCount = getTotalUnitsCount()

        return when {
            totalCount == 0 -> "no_units"
            availableCount == 0 -> "fully_rented"
            availableCount == totalCount -> "fully_available"
            else -> "partially_available"
        }
    }

    fun getStatusDisplayName(): String {
        return when (getVehicleStatus()) {
            "no_units" -> "No Units"
            "fully_rented" -> "Fully Rented"
            "fully_available" -> "Available"
            "partially_available" -> "Partially Available"
            else -> "Unknown"
        }
    }
}
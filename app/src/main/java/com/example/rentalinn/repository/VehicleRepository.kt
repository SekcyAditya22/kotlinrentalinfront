package com.example.rentalinn.repository

import com.example.rentalinn.model.Vehicle
import com.example.rentalinn.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.example.rentalinn.screens.admin.vehicle.VehicleUnitInput
import com.google.gson.Gson
import org.json.JSONArray

class VehicleRepository(private val apiService: ApiService) {

    // Get all vehicles
    fun getAllVehicles(): Flow<List<Vehicle>> = flow {
        println("DEBUG REPOSITORY: Making API call to getAllVehicles")
        val response = apiService.getAllVehicles()
        println("DEBUG REPOSITORY: Response code: ${response.code()}")

        if (response.isSuccessful && response.body() != null) {
            val vehicleData = response.body()!!.data
            println("DEBUG REPOSITORY: Successfully parsed ${vehicleData.size} vehicles")
            vehicleData.forEachIndexed { index, vehicle ->
                println("DEBUG REPOSITORY: Vehicle $index: ${vehicle.title} - ${vehicle.brand} ${vehicle.model}")
            }
            emit(vehicleData)
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Unknown error occurred"
            println("DEBUG REPOSITORY: Error response: $errorMsg")
            throw Exception(errorMsg)
        }
    }.flowOn(Dispatchers.IO)

    // Get vehicle by ID
    fun getVehicleById(id: Int): Flow<Vehicle> = flow {
        val response = apiService.getVehicleById(id)
        if (response.isSuccessful && response.body() != null) {
            emit(response.body()!!.data)
        } else {
            throw Exception(response.errorBody()?.string() ?: "Unknown error occurred")
        }
    }.flowOn(Dispatchers.IO)

    // Get vehicles with real-time availability
    fun getVehiclesWithAvailability(startDate: String? = null, endDate: String? = null): Flow<List<Vehicle>> = flow {
        println("DEBUG REPOSITORY: Making API call to getVehiclesWithAvailability")
        val response = apiService.getVehiclesWithAvailability(startDate, endDate)
        println("DEBUG REPOSITORY: Response code: ${response.code()}")

        if (response.isSuccessful && response.body() != null) {
            val vehicleData = response.body()!!.data
            println("DEBUG REPOSITORY: Successfully parsed ${vehicleData.size} vehicles with availability")
            vehicleData.forEachIndexed { index, vehicle ->
                println("DEBUG REPOSITORY: Vehicle $index: ${vehicle.title} - Available: ${vehicle.getAvailableUnits()}/${vehicle.getTotalUnitsCount()}")
            }
            emit(vehicleData)
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Unknown error occurred"
            println("DEBUG REPOSITORY: Error response: $errorMsg")
            throw Exception(errorMsg)
        }
    }.flowOn(Dispatchers.IO)

    // Get available vehicles
    fun getAvailableVehicles(): Flow<List<Vehicle>> = flow {
        val response = apiService.getAvailableVehicles()
        if (response.isSuccessful && response.body() != null) {
            emit(response.body()!!.data)
        } else {
            throw Exception(response.errorBody()?.string() ?: "Unknown error occurred")
        }
    }.flowOn(Dispatchers.IO)

    // Get vehicles by category
    fun getVehiclesByCategory(category: String): Flow<List<Vehicle>> = flow {
        val response = apiService.getVehiclesByCategory(category)
        if (response.isSuccessful && response.body() != null) {
            emit(response.body()!!.data)
        } else {
            throw Exception(response.errorBody()?.string() ?: "Unknown error occurred")
        }
    }.flowOn(Dispatchers.IO)

    // Create vehicle
    fun createVehicle(
        title: String,
        brand: String,
        model: String,
        vehicleCategory: String,
        year: Int,
        licensePlate: String,
        pricePerDay: Double,
        unit: Int,
        description: String?,
        transmission: String,
        fuelType: String,
        passengerCapacity: Int,
        features: List<String>?,
        photos: List<File>?
    ): Flow<Vehicle> = flow {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val brandPart = brand.toRequestBody("text/plain".toMediaTypeOrNull())
        val modelPart = model.toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryPart = vehicleCategory.toRequestBody("text/plain".toMediaTypeOrNull())
        val yearPart = year.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val licensePart = licensePlate.toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = pricePerDay.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val unitPart = unit.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
        val transmissionPart = transmission.toRequestBody("text/plain".toMediaTypeOrNull())
        val fuelTypePart = fuelType.toRequestBody("text/plain".toMediaTypeOrNull())
        val passengerPart = passengerCapacity.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        
        // Convert features list to proper JSON array string
        val featuresPart = features?.let {
            val jsonArray = JSONArray()
            features.forEach { feature -> jsonArray.put(feature) }
            jsonArray.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        
        val photoParts = photos?.map { file ->
            MultipartBody.Part.createFormData(
                "photos",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        } ?: listOf()
        
        val response = apiService.createVehicle(
            title = titlePart,
            brand = brandPart,
            model = modelPart,
            vehicleCategory = categoryPart,
            year = yearPart,
            licensePlate = licensePart,
            pricePerDay = pricePart,
            unit = unitPart,
            description = descriptionPart,
            transmission = transmissionPart,
            fuelType = fuelTypePart,
            passengerCapacity = passengerPart,
            features = featuresPart,
            photos = photoParts
        )
        
        if (response.isSuccessful && response.body() != null) {
            emit(response.body()!!.data)
        } else {
            throw Exception(response.errorBody()?.string() ?: "Unknown error occurred")
        }
    }.flowOn(Dispatchers.IO)

    // Create vehicle with units
    fun createVehicleWithUnits(
        title: String,
        brand: String,
        model: String,
        vehicleCategory: String,
        year: Int,
        pricePerDay: Double,
        description: String?,
        transmission: String,
        fuelType: String,
        passengerCapacity: Int,
        features: List<String>?,
        photos: List<File>?,
        units: List<VehicleUnitInput>
    ): Flow<Vehicle> = flow {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val brandPart = brand.toRequestBody("text/plain".toMediaTypeOrNull())
        val modelPart = model.toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryPart = vehicleCategory.toRequestBody("text/plain".toMediaTypeOrNull())
        val yearPart = year.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = pricePerDay.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
        val transmissionPart = transmission.toRequestBody("text/plain".toMediaTypeOrNull())
        val fuelTypePart = fuelType.toRequestBody("text/plain".toMediaTypeOrNull())
        val passengerPart = passengerCapacity.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val featuresPart = features?.joinToString(",")?.toRequestBody("text/plain".toMediaTypeOrNull())

        // Convert units to JSON string
        val unitsJson = Gson().toJson(units.map { unit ->
            mapOf(
                "plate_number" to unit.plateNumber,
                "current_location" to unit.currentLocation.takeIf { it.isNotBlank() },
                "notes" to unit.notes.takeIf { it.isNotBlank() }
            )
        })
        val unitsPart = unitsJson.toRequestBody("text/plain".toMediaTypeOrNull())

        val photoParts = photos?.map { file ->
            MultipartBody.Part.createFormData(
                "photos",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        } ?: listOf()

        val response = apiService.createVehicleWithUnits(
            title = titlePart,
            brand = brandPart,
            model = modelPart,
            vehicleCategory = categoryPart,
            year = yearPart,
            pricePerDay = pricePart,
            description = descriptionPart,
            transmission = transmissionPart,
            fuelType = fuelTypePart,
            passengerCapacity = passengerPart,
            features = featuresPart,
            units = unitsPart,
            photos = photoParts
        )

        if (response.isSuccessful && response.body() != null) {
            emit(response.body()!!.data)
        } else {
            throw Exception(response.errorBody()?.string() ?: "Unknown error occurred")
        }
    }.flowOn(Dispatchers.IO)

    // Update vehicle
    fun updateVehicle(
        id: Int,
        title: String? = null,
        brand: String? = null,
        model: String? = null,
        vehicleCategory: String? = null,
        year: Int? = null,
        licensePlate: String? = null,
        pricePerDay: Double? = null,
        unit: Int? = null,
        description: String? = null,
        status: String? = null,
        transmission: String? = null,
        fuelType: String? = null,
        passengerCapacity: Int? = null,
        features: List<String>? = null,
        photos: List<File>? = null
    ): Flow<Vehicle> = flow {
        println("DEBUG REPOSITORY: Starting vehicle update for ID: $id")
        
        // Create a map for all fields that will be sent
        val requestParts = HashMap<String, RequestBody>()
        
        // Add all non-null fields to the request
        title?.let { 
            println("DEBUG REPOSITORY: Adding title: $it")
            requestParts["title"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        brand?.let { 
            println("DEBUG REPOSITORY: Adding brand: $it")
            requestParts["brand"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        model?.let { 
            println("DEBUG REPOSITORY: Adding model: $it")
            requestParts["model"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        vehicleCategory?.let { 
            println("DEBUG REPOSITORY: Adding vehicle_category: $it")
            requestParts["vehicle_category"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        year?.let { 
            println("DEBUG REPOSITORY: Adding year: $it")
            requestParts["year"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        licensePlate?.let { 
            println("DEBUG REPOSITORY: Adding license_plate: $it")
            requestParts["license_plate"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        pricePerDay?.let { 
            println("DEBUG REPOSITORY: Adding price_per_day: $it")
            requestParts["price_per_day"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        unit?.let { 
            println("DEBUG REPOSITORY: Adding unit: $it")
            requestParts["unit"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        // Description can be empty string
        description?.let { 
            println("DEBUG REPOSITORY: Adding description: $it")
            requestParts["description"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        status?.let { 
            println("DEBUG REPOSITORY: Adding status: $it")
            requestParts["status"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        transmission?.let { 
            println("DEBUG REPOSITORY: Adding transmission: $it")
            requestParts["transmission"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        fuelType?.let { 
            println("DEBUG REPOSITORY: Adding fuel_type: $it")
            requestParts["fuel_type"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        passengerCapacity?.let { 
            println("DEBUG REPOSITORY: Adding passenger_capacity: $it")
            requestParts["passenger_capacity"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull()) 
        }
        
        // Convert features list to JSON array string
        features?.let { featuresList ->
            if (featuresList.isNotEmpty()) {
                val jsonArray = JSONArray()
                featuresList.forEach { feature -> jsonArray.put(feature) }
                println("DEBUG REPOSITORY: Adding features: $jsonArray")
                requestParts["features"] = jsonArray.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            }
        }
        
        // Only include photos if they are provided
        val photoParts = photos?.map { file ->
            println("DEBUG REPOSITORY: Adding photo: ${file.name}")
            MultipartBody.Part.createFormData(
                "photos",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        } ?: emptyList()
        
        // If we have photos to upload, add a flag to replace existing photos
        if (photos != null && photos.isNotEmpty()) {
            println("DEBUG REPOSITORY: Adding replace_photos flag: true")
            requestParts["replace_photos"] = "true".toRequestBody("text/plain".toMediaTypeOrNull())
        }
        
        // Make sure we have at least one field to update
        if (requestParts.isEmpty() && photoParts.isEmpty()) {
            println("DEBUG REPOSITORY: No fields to update!")
            throw Exception("No fields to update")
        }
        
        println("DEBUG REPOSITORY: Sending update request with ${requestParts.size} fields and ${photoParts.size} photos")
        
        try {
            val response = apiService.updateVehicle(
                id = id,
                parts = requestParts,
                photos = photoParts
            )
            
            if (response.isSuccessful && response.body() != null) {
                println("DEBUG REPOSITORY: Update successful: ${response.body()}")
                emit(response.body()!!.data)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                println("DEBUG REPOSITORY: Update failed: $errorBody")
                throw Exception(errorBody)
            }
        } catch (e: Exception) {
            println("DEBUG REPOSITORY: Exception during update: ${e.message}")
            throw e
        }
    }.flowOn(Dispatchers.IO)

    // Delete vehicle
    fun deleteVehicle(id: Int): Flow<Boolean> = flow {
        val response = apiService.deleteVehicle(id)
        if (response.isSuccessful) {
            emit(true)
        } else {
            throw Exception(response.errorBody()?.string() ?: "Unknown error occurred")
        }
    }.flowOn(Dispatchers.IO)
} 
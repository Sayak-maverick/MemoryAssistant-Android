package com.memoryassistant.data.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.IOException
import kotlin.coroutines.resume

/**
 * LocationService - GPS location tracking and reverse geocoding service
 *
 * This service handles:
 * 1. Getting current GPS location (latitude/longitude)
 * 2. Reverse geocoding (converting coordinates to address)
 * 3. Permission checking
 *
 * How it works:
 * 1. Check if location permission is granted
 * 2. Use FusedLocationProviderClient to get current location
 * 3. Use Geocoder to convert coordinates to human-readable address
 * 4. Return LocationData with all information
 *
 * Why GPS location?
 * - Helps users remember WHERE they last saw an item
 * - Useful for items that are frequently moved
 * - Can search items by location later
 */

/**
 * Data class to hold location information
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val locationName: String
)

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder: Geocoder = Geocoder(context)

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get current location with reverse geocoding
     *
     * This method:
     * 1. Checks permission
     * 2. Gets current GPS coordinates
     * 3. Converts coordinates to address
     * 4. Returns LocationData
     *
     * @return LocationData with coordinates and address, or null if failed
     */
    suspend fun getCurrentLocation(): LocationData? {
        // Check permission first
        if (!hasLocationPermission()) {
            return null
        }

        try {
            // Get current location using FusedLocationProviderClient
            // This is Google's recommended API for location on Android
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location == null) {
                return null
            }

            val latitude = location.latitude
            val longitude = location.longitude

            // Reverse geocode to get address
            val locationName = reverseGeocode(latitude, longitude)

            return LocationData(
                latitude = latitude,
                longitude = longitude,
                locationName = locationName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Reverse geocode coordinates to get human-readable address
     *
     * This converts GPS coordinates like (40.7128, -74.0060)
     * to an address like "New York, NY, USA"
     *
     * @param latitude - The latitude coordinate
     * @param longitude - The longitude coordinate
     * @return Human-readable address string
     */
    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Check if Geocoder is available
                if (!Geocoder.isPresent()) {
                    continuation.resume("${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}")
                    return@suspendCancellableCoroutine
                }

                // Android 33+ has a new async API for Geocoder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1  // Max results
                    ) { addresses ->
                        val address = addresses.firstOrNull()
                        continuation.resume(formatAddress(address, latitude, longitude))
                    }
                } else {
                    // Use deprecated synchronous API for older Android versions
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()
                    continuation.resume(formatAddress(address, latitude, longitude))
                }
            } catch (e: IOException) {
                // Network error or geocoding failed
                continuation.resume("${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}")
            } catch (e: Exception) {
                // Any other error
                continuation.resume("${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}")
            }
        }
    }

    /**
     * Format Address object into a readable string
     *
     * Priority:
     * 1. Feature name (e.g., "Starbucks")
     * 2. Street address (e.g., "123 Main St")
     * 3. City + State (e.g., "New York, NY")
     * 4. Country (e.g., "United States")
     * 5. Fallback to coordinates
     */
    private fun formatAddress(address: Address?, latitude: Double, longitude: Double): String {
        if (address == null) {
            return "${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}"
        }

        // Try to build a meaningful address string
        val parts = mutableListOf<String>()

        // Add feature name (e.g., building name, landmark)
        address.featureName?.let { if (it.isNotBlank() && it != address.subThoroughfare) parts.add(it) }

        // Add street address
        val street = buildString {
            address.subThoroughfare?.let { append("$it ") }  // House number
            address.thoroughfare?.let { append(it) }  // Street name
        }.trim()
        if (street.isNotBlank() && !parts.contains(street)) {
            parts.add(street)
        }

        // Add locality (city)
        address.locality?.let { if (it.isNotBlank()) parts.add(it) }

        // Add admin area (state)
        address.adminArea?.let { if (it.isNotBlank()) parts.add(it) }

        // Add country
        address.countryName?.let { if (it.isNotBlank() && parts.size < 2) parts.add(it) }

        // If we got something, join with commas
        if (parts.isNotEmpty()) {
            return parts.joinToString(", ")
        }

        // Fallback to coordinates
        return "${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}"
    }

    /**
     * Format coordinate to 4 decimal places
     * e.g., 40.712776 -> "40.7128"
     */
    private fun formatCoordinate(coordinate: Double): String {
        return "%.4f".format(coordinate)
    }
}

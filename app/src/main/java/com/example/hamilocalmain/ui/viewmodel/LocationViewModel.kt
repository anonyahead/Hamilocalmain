package com.example.hamilocalmain.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.*

/**
 * Manages device location tracking and geographic utility functions.
 */
class LocationViewModel : ViewModel() {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _currentAddress = MutableStateFlow("")
    val currentAddress: StateFlow<String> = _currentAddress.asStateFlow()

    /**
     * Requests the current device location using FusedLocationProviderClient.
     * Updates [currentLocation] and performs reverse geocoding to update [currentAddress].
     * 
     * @param context The context used to access system location services.
     */
    @SuppressLint("MissingPermission")
    fun requestLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        viewModelScope.launch {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    _currentLocation.value = location
                    if (location != null) {
                        updateAddress(context, location.latitude, location.longitude)
                    }
                }
        }
    }

    /**
     * Performs reverse geocoding to convert coordinates into a human-readable address string.
     */
    private fun updateAddress(context: Context, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val addressParts = mutableListOf<String>()
                            for (i in 0..address.maxAddressLineIndex) {
                                addressParts.add(address.getAddressLine(i))
                            }
                            _currentAddress.value = addressParts.joinToString(", ")
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressParts = mutableListOf<String>()
                        for (i in 0..address.maxAddressLineIndex) {
                            addressParts.add(address.getAddressLine(i))
                        }
                        _currentAddress.value = addressParts.joinToString(", ")
                    }
                }
            } catch (e: Exception) {
                _currentAddress.value = "Address not found"
            }
        }
    }

    /**
     * Calculates distance between two coordinates using Haversine formula.
     * 
     * @param lat1 Latitude of first point.
     * @param lng1 Longitude of first point.
     * @param lat2 Latitude of second point.
     * @param lng2 Longitude of second point.
     * @return Distance in kilometers.
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

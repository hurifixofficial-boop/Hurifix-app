package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {
    private const val TAG = "LocationHelper"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Calculates distance in kilometers between two coordinates using the Haversine formula.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c * 1.3
    }

    /**
     * Estimates travel time in minutes based on urban technician bike travel (average 24 km/h + 3 min traffic/prep buffer).
     */
    fun estimateTravelTimeMinutes(distanceKm: Double): Int {
        if (distanceKm <= 0.2) return 2
        val avgSpeedKmh = 24.0
        val travelHours = distanceKm / avgSpeedKmh
        val minutes = (travelHours * 60).toInt() + 3
        return minutes.coerceAtLeast(3)
    }

    /**
     * Formats distance nicely (e.g. "850 m" or "2.4 km").
     */
    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            val meters = (distanceKm * 1000).toInt()
            "$meters m"
        } else {
            String.format(Locale.US, "%.1f km", distanceKm)
        }
    }

    /**
     * Generates a direct Google Maps pin link.
     */
    fun createGoogleMapsUrl(latitude: Double, longitude: Double): String {
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }

    /**
     * Checks if the text looks like a Google Maps link.
     */
    fun isGoogleMapsUrl(input: String): Boolean {
        val lower = input.lowercase().trim()
        return lower.contains("maps.google.") ||
                lower.contains("google.com/maps") ||
                lower.contains("goo.gl/maps") ||
                lower.contains("maps.app.goo.gl")
    }

    /**
     * Smart parser to extract Latitude and Longitude from raw text, coordinates, or Google Maps URL.
     * Supports formats like:
     * - "28.5708, 77.3261"
     * - "https://maps.google.com/?q=28.5708,77.3261"
     * - "https://maps.google.com/?q=28.5708+77.3261"
     * - "https://www.google.com/maps/@28.5708,77.3261,15z"
     * - "https://www.google.com/maps/place/28.5708,77.3261"
     * - "https://www.google.com/maps/search/28.5708,77.3261"
     * - "https://www.google.com/maps/place/.../!3d28.5708!4d77.3261"
     * - "lat: 28.5708 lng: 77.3261"
     */
    fun parseCoordinatesFromText(input: String): Pair<Double, Double>? {
        if (input.isBlank()) return null

        val textToProcess = listOf(
            input,
            try { URLDecoder.decode(input, "UTF-8") } catch (_: Exception) { input }
        )

        for (text in textToProcess) {
            // 1. Check for query parameters like q=lat,lng or query=lat,lng or ll=lat,lng or daddr=lat,lng
            val urlQueryPattern = Pattern.compile("[?&](?:q|query|ll|daddr|saddr|destination)=([+-]?\\d+\\.\\d+)[,\\s+]+([+-]?\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE)
            val urlMatcher = urlQueryPattern.matcher(text)
            if (urlMatcher.find()) {
                val lat = urlMatcher.group(1)?.toDoubleOrNull()
                val lng = urlMatcher.group(2)?.toDoubleOrNull()
                if (isValidLatLng(lat, lng)) return Pair(lat!!, lng!!)
            }

            // 2. Check for @lat,lng in google maps URLs
            val atPattern = Pattern.compile("@([+-]?\\d+\\.\\d+)[,\\s+]+([+-]?\\d+\\.\\d+)")
            val atMatcher = atPattern.matcher(text)
            if (atMatcher.find()) {
                val lat = atMatcher.group(1)?.toDoubleOrNull()
                val lng = atMatcher.group(2)?.toDoubleOrNull()
                if (isValidLatLng(lat, lng)) return Pair(lat!!, lng!!)
            }

            // 3. Check for !3d<lat>!4d<lng> format in web Google Maps URLs
            val d3d4Pattern = Pattern.compile("!3d([+-]?\\d+\\.\\d+)!4d([+-]?\\d+\\.\\d+)")
            val d3d4Matcher = d3d4Pattern.matcher(text)
            if (d3d4Matcher.find()) {
                val lat = d3d4Matcher.group(1)?.toDoubleOrNull()
                val lng = d3d4Matcher.group(2)?.toDoubleOrNull()
                if (isValidLatLng(lat, lng)) return Pair(lat!!, lng!!)
            }

            // 4. Check for /place/lat,lng or /search/lat,lng or /dir//lat,lng
            val pathPattern = Pattern.compile("/(?:place|search|dir/[^/]*)/([+-]?\\d+\\.\\d+)[,\\s+]+([+-]?\\d+\\.\\d+)")
            val pathMatcher = pathPattern.matcher(text)
            if (pathMatcher.find()) {
                val lat = pathMatcher.group(1)?.toDoubleOrNull()
                val lng = pathMatcher.group(2)?.toDoubleOrNull()
                if (isValidLatLng(lat, lng)) return Pair(lat!!, lng!!)
            }

            // 5. Check for geo:lat,lng
            val geoPattern = Pattern.compile("geo:([+-]?\\d+\\.\\d+)[,\\s+]+([+-]?\\d+\\.\\d+)")
            val geoMatcher = geoPattern.matcher(text)
            if (geoMatcher.find()) {
                val lat = geoMatcher.group(1)?.toDoubleOrNull()
                val lng = geoMatcher.group(2)?.toDoubleOrNull()
                if (isValidLatLng(lat, lng)) return Pair(lat!!, lng!!)
            }

            // 6. Check for labeled lat/lng format like "lat: 28.5708, lng: 77.3261"
            val labeledPattern = Pattern.compile("(?:lat|latitude)[:=\\s]+([+-]?\\d+\\.\\d+)[^0-9+-]+(?:lon|lng|longitude)[:=\\s]+([+-]?\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE)
            val labeledMatcher = labeledPattern.matcher(text)
            if (labeledMatcher.find()) {
                val lat = labeledMatcher.group(1)?.toDoubleOrNull()
                val lng = labeledMatcher.group(2)?.toDoubleOrNull()
                if (isValidLatLng(lat, lng)) return Pair(lat!!, lng!!)
            }

            // 7. Check for standard raw "28.1234, 77.5678" or "28.1234 77.5678"
            val generalPattern = Pattern.compile("([+-]?\\d{1,2}\\.\\d{2,12})\\s*[,\\s]\\s*([+-]?\\d{1,3}\\.\\d{2,12})")
            val generalMatcher = generalPattern.matcher(text)
            if (generalMatcher.find()) {
                val lat = generalMatcher.group(1)?.toDoubleOrNull()
                val lng = generalMatcher.group(2)?.toDoubleOrNull()
                if (isValidLatLng(lat, lng)) return Pair(lat!!, lng!!)
            }
        }

        return null
    }

    private fun isValidLatLng(lat: Double?, lng: Double?): Boolean {
        if (lat == null || lng == null) return false
        if (lat == 0.0 && lng == 0.0) return false
        return lat in -90.0..90.0 && lng in -180.0..180.0
    }

    /**
     * Resolves Google Maps URLs (including shortened links like goo.gl/maps or maps.app.goo.gl)
     * by following redirects and extracting the destination coordinates.
     */
    suspend fun resolveAndExtractCoordinates(input: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val direct = parseCoordinatesFromText(input)
        if (direct != null) return@withContext direct

        val trimmed = input.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            try {
                val request = Request.Builder()
                    .url(trimmed)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:100.0) Gecko/100.0 Firefox/100.0")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val finalUrl = response.request.url.toString()
                    val redirectedParsed = parseCoordinatesFromText(finalUrl)
                    if (redirectedParsed != null) return@withContext redirectedParsed

                    // If redirected URL did not have coordinates in query, check response body
                    val bodyString = response.body?.string() ?: ""
                    val bodyParsed = parseCoordinatesFromText(bodyString)
                    if (bodyParsed != null) return@withContext bodyParsed
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error resolving shortened Maps URL: ${e.message}")
            }
        }
        null
    }

    /**
     * Checks if ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is granted.
     */
    fun isLocationPermissionGranted(context: Context): Boolean {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Checks if device GPS / location provider is enabled in system settings.
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            ?: return false
        val gpsEnabled = try {
            locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
        val networkEnabled = try {
            locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
        return gpsEnabled || networkEnabled
    }

    /**
     * Opens Google Maps for given coordinates.
     */
    fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "Location") {
        try {
            val uri = android.net.Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val webUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, webUri).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    fun openGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "Location") {
        openInGoogleMaps(context, latitude, longitude, label)
    }

    fun formatEstimatedTravelTime(distanceKm: Double): String {
        val minutes = estimateTravelTimeMinutes(distanceKm)
        return "$minutes mins"
    }

    fun calculateTaskDuration(createdAt: Long, completedAt: Long?): String {
        val end = completedAt ?: System.currentTimeMillis()
        val diffMs = (end - createdAt).coerceAtLeast(0)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val hours = minutes / 60
        val days = hours / 24
        return when {
            days > 0 -> "$days d ${hours % 24} h"
            hours > 0 -> "$hours h ${minutes % 60} m"
            else -> "${minutes.coerceAtLeast(1)} mins"
        }
    }

    /**
     * Opens Application Details in system settings to let user grant location permission.
     */
    fun openAppSettings(context: Context) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Opens Device Location Settings so user can toggle GPS on.
     */
    fun openLocationSettings(context: Context) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Gets device current GPS location if location permission is granted.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                onError("Location manager unavailable")
                return
            }
            val gpsLocation = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (_: Exception) {
                null
            }

            val networkLocation = try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (_: Exception) {
                null
            }

            val bestLocation = gpsLocation ?: networkLocation
            if (bestLocation != null) {
                onSuccess(bestLocation)
            } else {
                onError("GPS location not available, using default city center.")
            }
        } catch (e: Exception) {
            onError("GPS Error: ${e.message}")
        }
    }
}

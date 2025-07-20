package com.mral.LocationHelper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

// 🔽 Pour l'envoi HTTP + JSON
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.io.BufferedReader
import java.io.InputStreamReader

// 🔽 Pour les logs
import android.util.Log

object LocationHelper {

   // private const val BASE_URL = "https://geektestgo.onrender.com"  
    private const val BASE_URL = "https://testrust-4io8.onrender.com"
    
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onLocationResult: (Location?) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            Toast.makeText(context, "Permission de localisation manquante", Toast.LENGTH_SHORT).show()
            onLocationResult(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationResult(location)
            } else {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    0L
                ).setMaxUpdates(1).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        onLocationResult(result.lastLocation)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Erreur lors de la récupération de la position", Toast.LENGTH_SHORT).show()
            onLocationResult(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        locationUpdateIntervalMs: Long = 1000L,
        onLocationUpdate: (Location) -> Unit
    ): LocationCallback? {
        if (!hasLocationPermission(context)) {
            Toast.makeText(context, "Permission de localisation manquante", Toast.LENGTH_SHORT).show()
            return null
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            locationUpdateIntervalMs
        )
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(1f)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onLocationUpdate(it) }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return locationCallback
    }

    fun stopLocationUpdates(
        fusedLocationClient: FusedLocationProviderClient,
        locationCallback: LocationCallback?
    ) {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    fun sendPositionToServer(busNumber: String, latitude: Double, longitude: Double) {
        val json = JSONObject().apply {
            put("busNumber", busNumber)
            put("latitude", latitude)
            put("longitude", longitude)
        }

        Thread {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/api/position")
                connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                connection.outputStream.use { outputStream ->
                    val writer = OutputStreamWriter(outputStream, "UTF-8")
                    writer.write(json.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val inputStream = if (responseCode < 400) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = inputStream?.bufferedReader()?.use { it.readText() } ?: ""

                Log.d("API", "Code: $responseCode, Réponse: $response")

                if (responseCode in 200..299) {
                    Log.d("API", "Position envoyée avec succès pour le bus $busNumber")
                } else {
                    Log.e("API", "Erreur serveur: $responseCode - $response")
                }

            } catch (e: Exception) {
                Log.e("API", "Erreur d'envoi pour le bus $busNumber: ${e.message}", e)
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    fun stopSharingOnServer(busNumber: String) {
        val json = JSONObject().apply {
            put("busNumber", busNumber)
        }

        Thread {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/api/stopSharing")
                connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                connection.outputStream.use { outputStream ->
                    val writer = OutputStreamWriter(outputStream, "UTF-8")
                    writer.write(json.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    Log.d("API", "Arrêt du partage signalé au serveur pour bus $busNumber")
                } else {
                    Log.e("API", "Erreur serveur lors de l'arrêt du partage: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("API", "Erreur réseau lors de l'arrêt du partage: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }.start()
    }
}
package androidtown.org.moveon.map


import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class LocationTracker(private val locationClient: FusedLocationProviderClient) {

    private var locationCallback: LocationCallback? = null

    fun startLocationUpdates(onLocationReceived: (Location) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000L
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { onLocationReceived(it) }
            }
        }

        locationCallback?.let {
            locationClient.requestLocationUpdates(locationRequest, it, null)
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let { locationClient.removeLocationUpdates(it) }
    }
}

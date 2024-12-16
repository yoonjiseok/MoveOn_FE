package androidtown.org.moveon

import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MainRecordFragment : Fragment(R.layout.fragment_main_record), OnMapReadyCallback, SensorEventListener {

    private var isTimerRunning = false
    private var isDistanceTracking = false
    private var timeInSeconds = 0
    private var totalDistance = 0f
    private var totalSteps = 0 // 걸음 수
    private lateinit var lastLocation: Location
    private lateinit var handler: Handler

    private lateinit var timeValueText: TextView
    private lateinit var distanceValueText: TextView
    private lateinit var stepValueText: TextView
    private lateinit var playButton: ImageView
    private lateinit var pauseButton: ImageView
    private lateinit var stopButton: ImageView

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private val sharedMapViewModel: SharedMapViewModel by activityViewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화
        timeValueText = view.findViewById(R.id.timeValue)
        distanceValueText = view.findViewById(R.id.distance_value)
        stepValueText = view.findViewById(R.id.step_count)
        playButton = view.findViewById(R.id.play_button)
        pauseButton = view.findViewById(R.id.pause_button)
        stopButton = view.findViewById(R.id.stop_button)

        pauseButton.visibility = View.GONE
        stopButton.visibility = View.GONE

        handler = Handler(Looper.getMainLooper())

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 센서 매니저 초기화
        sensorManager = requireContext().getSystemService(SensorManager::class.java)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // 지도 설정
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 버튼 클릭 이벤트
        playButton.setOnClickListener {
            startTimer()
            startDistanceTracking()
            startStepCounting()
            playButton.visibility = View.GONE
            pauseButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
        }

        pauseButton.setOnClickListener {
            pauseTimer()
            pauseDistanceTracking()
            pauseStepCounting()
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE
        }

        stopButton.setOnClickListener {
            val finalTime = timeInSeconds
            val finalDistance = totalDistance
            val finalSteps = totalSteps
            stopTimer()
            stopDistanceTracking()
            stopStepCounting()
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE
            stopButton.visibility = View.GONE

            navigateToRecordRunningActivity(finalTime, finalDistance, finalSteps)
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        sharedMapViewModel.cameraPosition.observe(viewLifecycleOwner) { position ->
            val (latLng, zoom) = position
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isTimerRunning && event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            totalSteps++
            stepValueText.text = "$totalSteps"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    lastLocation = it
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    sharedMapViewModel.updateCameraPosition(currentLatLng, 15f)
                }
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            handler.post(timerRunnable)
        }
    }

    private fun pauseTimer() {
        isTimerRunning = false
        handler.removeCallbacks(timerRunnable)
    }

    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacks(timerRunnable)
        updateTimerText()
    }

    private fun startDistanceTracking() {
        isDistanceTracking = true
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).apply {
            setMinUpdateIntervalMillis(1000)
        }.build()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                        val newLocation = result.lastLocation
                        if (newLocation != null && ::lastLocation.isInitialized && isDistanceTracking) {
                            if (newLocation.accuracy <= 10) {
                                val distance = lastLocation.distanceTo(newLocation)
                                if (distance > 5) {
                                    totalDistance += distance
                                    distanceValueText.text = String.format("%.2f m", totalDistance)
                                    lastLocation = newLocation
                                }
                            }
                        } else if (newLocation != null) {
                            lastLocation = newLocation
                        }
                    }
                },
                Looper.getMainLooper()
            )
        }
    }

    private fun pauseDistanceTracking() {
        isDistanceTracking = false
    }

    private fun stopDistanceTracking() {
        isDistanceTracking = false
        fusedLocationClient.removeLocationUpdates(object : com.google.android.gms.location.LocationCallback() {})
    }

    private fun startStepCounting() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun pauseStepCounting() {
        sensorManager.unregisterListener(this, stepSensor)
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this, stepSensor)
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                timeInSeconds++
                updateTimerText()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun updateTimerText() {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        val hours = timeInSeconds / 3600
        timeValueText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun navigateToRecordRunningActivity(finalTime: Int, finalDistance: Float, finalSteps: Int) {
        val intent = Intent(requireContext(), RecordRunningActivity::class.java)
        intent.putExtra("RUNNING_TIME", finalTime)
        intent.putExtra("TOTAL_DISTANCE", finalDistance)
        intent.putExtra("TOTAL_STEPS", finalSteps)
        startActivity(intent)
    }
}

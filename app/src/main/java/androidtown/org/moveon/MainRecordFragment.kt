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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import androidtown.org.moveon.map.GridManager
import androidx.fragment.app.activityViewModels

class MainRecordFragment : Fragment(R.layout.fragment_main_record), OnMapReadyCallback, SensorEventListener {

    private var isTimerRunning = false
    private var isDistanceTracking = false
    private var timeInSeconds = 0
    private var totalDistance = 0f
    private var totalPixel = 0
    private var totalSteps = 0 // 걸음 수
    private lateinit var lastLocation: Location
    private lateinit var handler: Handler

    private lateinit var timeValueText: TextView
    private lateinit var currentGridCountText: TextView
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
    private lateinit var gridManager: GridManager
    private lateinit var locationCallback: LocationCallback


    private val pathPoints = mutableListOf<LatLng>() // 경로 데이터를 저장

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화
        timeValueText = view.findViewById(R.id.timeValue)
        distanceValueText = view.findViewById(R.id.distance_value)
        stepValueText = view.findViewById(R.id.step_count)
        currentGridCountText = view.findViewById(R.id.currentGridCountText)
        playButton = view.findViewById(R.id.play_button)
        pauseButton = view.findViewById(R.id.pause_button)
        stopButton = view.findViewById(R.id.stop_button)

        pauseButton.visibility = View.GONE
        stopButton.visibility = View.GONE

        handler = Handler(Looper.getMainLooper())

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        gridManager = GridManager(gridSize = 0.00045)
        // 센서 매니저 초기화
        sensorManager = requireContext().getSystemService(SensorManager::class.java)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // 지도 설정
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 버튼 클릭 이벤트
        playButton.setOnClickListener {
            startTimer()
            startLocationUpdates()
            startDistanceTracking()
            startStepCounting()
            playButton.visibility = View.GONE
            pauseButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
        }

        pauseButton.setOnClickListener {
            pauseTimer()
            stopLocationUpdates()
            pauseDistanceTracking()
            pauseStepCounting()
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE
        }

        stopButton.setOnClickListener {
            val finalTime = timeInSeconds
            val finalDistance = totalDistance
            val finalSteps = totalSteps
            val finalPixel = totalPixel
            stopTimer()
            stopLocationUpdates()
            stopDistanceTracking()
            stopStepCounting()
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE
            stopButton.visibility = View.GONE

            navigateToRecordRunningActivity(finalTime, finalDistance, finalSteps, finalPixel)
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
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                    sharedMapViewModel.updateCameraPosition(currentLatLng, 17f)
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

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(1000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    gridManager.markGridAsVisited(mMap, userLocation)

                    val visitedCount = gridManager.getVisitedCount()
                    totalPixel = visitedCount
                    currentGridCountText.text = totalPixel.toString()

                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
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


    private fun pauseDistanceTracking() {
        isDistanceTracking = false
    }

    private fun stopDistanceTracking() {
        isDistanceTracking = false
        fusedLocationClient.removeLocationUpdates(object :
            com.google.android.gms.location.LocationCallback() {})
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

    //칼로리 계산
    private fun calculateCalories(distanceInMeters: Float, weightKg: Float = 60f): Int {
        val distanceKm = distanceInMeters / 1000
        return (distanceKm * weightKg * 1.036f).toInt()
    }

    private fun startDistanceTracking() {
        isDistanceTracking = true
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { newLocation ->
                            val latLng = LatLng(newLocation.latitude, newLocation.longitude)
                            if (::lastLocation.isInitialized && isDistanceTracking) {
                                val distance = lastLocation.distanceTo(newLocation)
                                if (distance > 0.5) {
                                    totalDistance += distance
                                    distanceValueText.text = String.format("%.2f m", totalDistance)
                                }
                            }
                            pathPoints.add(latLng) // 경로 추가
                            lastLocation = newLocation
                        }
                    }
                },
                Looper.getMainLooper()
            )
        }
    }


    private fun generateStaticMapUrl(): String {
        val apiKey = "AIzaSyCLYJJKmvriJ5WvzKNfqGvizryUda8FZJY" // Google Maps API 키
        val path = pathPoints.joinToString("|") { "${it.latitude},${it.longitude}" }
        val size = "600x300" // 이미지 사이즈
        return "https://maps.googleapis.com/maps/api/staticmap?size=$size&path=color:0x0000ff|weight:5|$path&key=$apiKey"
    }


    private fun navigateToRecordRunningActivity(
        finalTime: Int,
        finalDistance: Float,
        finalSteps: Int,
        finalPixel: Int
    ) {
        val staticMapUrl = generateStaticMapUrl() // Static Map URL 생성
        val caloriesBurned = calculateCalories(finalDistance)//칼로리 계산
        val intent = Intent(requireContext(), RecordRunningActivity::class.java).apply {
            putExtra("RUNNING_TIME", finalTime)
            putExtra("TOTAL_DISTANCE", finalDistance)
            putExtra("TOTAL_PIXEL", finalPixel)
            putExtra("TOTAL_STEPS", finalSteps)
            putExtra("CALORIES_BURNED", caloriesBurned)
            putExtra("MAP_IMAGE_URL", staticMapUrl) // URL 전달
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

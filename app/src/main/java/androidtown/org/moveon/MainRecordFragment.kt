package androidtown.org.moveon

import android.app.AlertDialog
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
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
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
    private var lastSpeedCheckTime = System.currentTimeMillis() - 6000L // 마지막 속도 체크 시간을 6초 전으로 초기화
    private var isSpeedAlertShowing = false // 속도 경고 알림이 현재 표시 중인지
    private lateinit var lastLocation: Location
    private lateinit var handler: Handler

    private lateinit var timeValueText: TextView
    private lateinit var currentGridCountText: TextView
    private lateinit var distanceValueText: TextView
    private lateinit var stepValueText: TextView
    private lateinit var currentSpeedText: TextView
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
    private lateinit var refreshIcon: ImageView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
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

    private val distanceLocationCallback = object : LocationCallback() {
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화
        timeValueText = view.findViewById(R.id.timeValue)
        distanceValueText = view.findViewById(R.id.distance_value)
        stepValueText = view.findViewById(R.id.step_count)
        currentGridCountText = view.findViewById(R.id.currentGridCountText)
        currentSpeedText = view.findViewById(R.id.currentSpeedText)
        playButton = view.findViewById(R.id.play_button)
        pauseButton = view.findViewById(R.id.pause_button)
        stopButton = view.findViewById(R.id.stop_button)

        pauseButton.visibility = View.GONE
        stopButton.visibility = View.GONE

        refreshIcon = view.findViewById(R.id.refresh_icon)
        refreshIcon.setOnClickListener{
            showFilterPopup()
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

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
                android.util.Log.d("LocationUpdate", "위치 업데이트 받음")
                for (location in result.locations) {
                    android.util.Log.d("LocationUpdate",
                        "새 위치: (${location.latitude}, ${location.longitude})")
                    // 실시간 속도 계산 및 표시
                    val speedKmh = if (location.hasSpeed()) {
                        location.speed * 3.6f  // m/s를 km/h로 변환
                    } else {
                        0f
                    }
                    
                    // UI 업데이트는 메인 스레드에서 실행
                    activity?.runOnUiThread {
                        currentSpeedText.text = String.format("%.1f km/h", speedKmh)
                    }
                    
                    // 현재 시간 체크
                    val currentTime = System.currentTimeMillis()
                    
                    if (!isSpeedAlertShowing &&
                        currentTime - lastSpeedCheckTime >= 5000 && // 5초로 변경
                        ::lastLocation.isInitialized
                    ) {
                        val distance = location.distanceTo(lastLocation)
                        val timeDiff = (currentTime - lastSpeedCheckTime) / 1000f
                        val calculatedSpeed = (distance / timeDiff) * 3.6f
                        android.util.Log.d(
                            "SpeedCheck",
                            """
                            속도 체크:
                            - 현재 속도: $speedKmh km/h
                            - 속도 제한: 20 km/h
                            - 알림 표시 가능: ${!isSpeedAlertShowing}
                            - 시간 체크: ${currentTime - lastSpeedCheckTime >= 5000}ms 경과 (5초 간격)
                            """
                        )
                        
                        // GPS 속도만으로 판단
                        if (location.hasSpeed() && speedKmh > 20) {
                            android.util.Log.d("SpeedCheck", "속도 초과 감지! 알림 표시")
                            lastSpeedCheckTime = currentTime
                            isSpeedAlertShowing = true
                            
                            pauseTimer()
                            
                            android.util.Log.d("SpeedCheck", "타이머 일시정지 완료, 알림 표시 시작")
                         
                            val currentActivity = activity
                            if (currentActivity != null) {
                               android.util.Log.d("SpeedCheck", "액티비티 확인됨, 다이얼로그 생성 시작")
                               currentActivity.runOnUiThread {
                                   android.util.Log.d("SpeedCheck", "UI 스레드에서 다이얼로그 생성 중")
                                   val dialogView = LayoutInflater.from(requireContext())
                                       .inflate(R.layout.dialog_speed_warning, null)

                                   // 직접 TextView 참조하여 텍스트 설정
                                   dialogView.findViewById<TextView>(R.id.dialogTitle)?.let { titleView ->
                                       titleView.text = "혹시 자동차나 자전거를 타고 계시나요?"
                                       titleView.visibility = View.VISIBLE
                                       android.util.Log.d("SpeedCheck", "제목 설정됨: ${titleView.text}")
                                   } ?: android.util.Log.e("SpeedCheck", "제목 TextView를 찾을 수 없음")
                                   
                                   dialogView.findViewById<TextView>(R.id.dialogMessage)?.let { messageView ->
                                       messageView.text = "속도가 너무 빠르면 기록이 일시정지 됩니다.\n\"Move on\"은 러닝 앱으로\n걷거나 뛰어야 기록이 됩니다."
                                       messageView.visibility = View.VISIBLE
                                       android.util.Log.d("SpeedCheck", "메시지 설정됨: ${messageView.text}")
                                   } ?: android.util.Log.e("SpeedCheck", "메시지 TextView를 찾을 수 없음")

                                   val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                                    .setView(dialogView)
                                    .setCancelable(false)
                                    .create()

                                dialogView.findViewById<Button>(R.id.stopButton).setOnClickListener {
                                    dialog.dismiss()
                                    isSpeedAlertShowing = false
                                    val finalTime = timeInSeconds
                                    val finalDistance = totalDistance
                                    val finalSteps = totalSteps
                                    val finalPixel = totalPixel
                                    
                                    stopTimer()
                                    stopLocationUpdates()
                                    stopDistanceTracking()
                                    stopStepCounting()
                                    
                                    navigateToRecordRunningActivity(finalTime, finalDistance, finalSteps, finalPixel)
                                }

                                dialogView.findViewById<Button>(R.id.continueButton).setOnClickListener {
                                    dialog.dismiss()
                                    isSpeedAlertShowing = false
                                    startTimer()
                                }

                                dialog.setOnDismissListener {
                                    isSpeedAlertShowing = false
                                }

                                   dialog.show()
                                   android.util.Log.d("SpeedCheck", "다이얼로그 표시됨")
                               }
                           } else {
                               android.util.Log.e("SpeedCheck", "액티비티가 null이어서 다이얼로그를 표시할 수 없음")
                           }
                        }
                    }
                    
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

    private fun updateTimerText() {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        val hours = timeInSeconds / 3600
        timeValueText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun pauseDistanceTracking() {
        isDistanceTracking = false
    }

    private fun stopDistanceTracking() {
        isDistanceTracking = false
        fusedLocationClient.removeLocationUpdates(distanceLocationCallback)
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
                distanceLocationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun generateStaticMapUrl(): String {
        val apiKey = requireContext().getString(R.string.google_maps_key)
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

    private fun showFilterPopup() {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_filter, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(popupView)
            .setCancelable(true) // 외부 터치 시 팝업 닫기
            .create()

        val personalRecordButton: LinearLayout = popupView.findViewById(R.id.personal_record)
        val friendViewButton: LinearLayout = popupView.findViewById(R.id.friend_view)

        var personalRecordBackground = R.drawable.personal_record_selector
        var friendViewBackground = R.drawable.friend_view_selector

        personalRecordButton.setOnClickListener {
            if (personalRecordBackground == R.drawable.friend_view_selector) {
                personalRecordButton.setBackgroundResource(friendViewBackground)
                friendViewButton.setBackgroundResource(personalRecordBackground)

                val temp = personalRecordBackground
                personalRecordBackground = friendViewBackground
                friendViewBackground = temp
            }
        }

        friendViewButton.setOnClickListener {
            if (friendViewBackground == R.drawable.friend_view_selector) {
                personalRecordButton.setBackgroundResource(friendViewBackground)
                friendViewButton.setBackgroundResource(personalRecordBackground)

                val temp = personalRecordBackground
                personalRecordBackground = friendViewBackground
                friendViewBackground = temp
            }
        }
        dialog.show()
    }
}

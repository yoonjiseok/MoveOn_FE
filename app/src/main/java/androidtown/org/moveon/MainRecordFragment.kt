package androidtown.org.moveon

import android.content.Intent
import android.content.pm.PackageManager
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

class MainRecordFragment : Fragment(R.layout.fragment_main_record), OnMapReadyCallback {

    private var isTimerRunning = false
    private var timeInSeconds = 0
    private lateinit var handler: Handler

    private lateinit var timeValueText: TextView
    private lateinit var playButton: ImageView
    private lateinit var pauseButton: ImageView
    private lateinit var stopButton: ImageView

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val sharedMapViewModel: SharedMapViewModel by activityViewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        timeValueText = view.findViewById(R.id.timeValue)
        playButton = view.findViewById(R.id.play_button)
        pauseButton = view.findViewById(R.id.pause_button)
        stopButton = view.findViewById(R.id.stop_button)

        // Initialize button visibility
        pauseButton.visibility = View.GONE
        stopButton.visibility = View.GONE

        handler = Handler(Looper.getMainLooper())

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 지도 프래그먼트 설정
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 타이머 버튼 이벤트
        playButton.setOnClickListener {
            startTimer()
            playButton.visibility = View.GONE
            pauseButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
        }

        pauseButton.setOnClickListener {
            pauseTimer()
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE
        }

        stopButton.setOnClickListener {
            val finalTime = timeInSeconds
            stopTimer()
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE
            stopButton.visibility = View.GONE

            navigateToRecordRunningActivity(finalTime)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // ViewModel에 저장된 지도 상태를 반영
        sharedMapViewModel.cameraPosition.observe(viewLifecycleOwner) { position ->
            val (latLng, zoom) = position
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }

        // 위치 권한 확인 및 현재 위치 활성화
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }

        // 지도 UI 설정
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // 카메라 이동 이벤트 리스너 추가
        mMap.setOnCameraIdleListener {
            val cameraPosition = mMap.cameraPosition
            sharedMapViewModel.updateCameraPosition(cameraPosition.target, cameraPosition.zoom)
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

    private fun navigateToRecordRunningActivity(finalTime: Int) {
        try {
            val intent = Intent(requireContext(), RecordRunningActivity::class.java)
            intent.putExtra("RUNNING_TIME", finalTime)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
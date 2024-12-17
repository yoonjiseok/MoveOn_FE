package androidtown.org.moveon.map

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidtown.org.moveon.MainRecordFragment
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import androidtown.org.moveon.R

class MapFragment : Fragment(R.layout.fragment_running), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gridManager: GridManager
    private lateinit var locationCallback: LocationCallback

    private var isTrackingStarted = false
    private lateinit var startButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        gridManager = GridManager(gridSize = 0.00045) // 격자 크기 설정 (약 50m)

        // 권한 확인 및 위치 가져오기
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            moveToCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    private fun moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        // 마지막 위치를 가져오고 없으면 새 위치 요청
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                    startLocationUpdates()
                } else {
                    requestNewLocation()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "위치를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestNewLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                    gridManager.markGridAsVisited(googleMap, currentLatLng)
                    break // 첫 번째 위치만 사용
                }
                fusedLocationClient.removeLocationUpdates(this) // 초기 위치 이후 중지
                startLocationUpdates() // 지속적으로 업데이트 시작
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
                requireActivity().mainLooper
            )
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    gridManager.markGridAsVisited(googleMap, userLocation)
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
                requireActivity().mainLooper
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 위치 업데이트 중지 (메모리 누수 방지)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

package com.example.test_driver2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.test_driver2.R  // 올바른 R 파일 임포트 확인
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageBoardingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var locationService: LocationService
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var bodyDestinationTextView: TextView
    private lateinit var bodyPassengerTextView: TextView
    private lateinit var nextstation: TextView

    private val driverId = "3f6941e6-835e-4c62-9f4b-b49617cf312e"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private var currentDeparture: String? = null
    private var currentDestination: String? = null
    private var isDepartingFromTukorea = true  // 출발지 상태 (초기값은 한국공학대학교 정문)
    private var currentLocation: LatLng? = null  // 현재 위치

    // supabase 상태 연결해서 사용하세요
    private var departureName = "한국공학대학교 정문"
    private var destinationName = "정왕역"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationService = LocationService(requireContext()) // requireContext() 사용
        locationSource = FusedLocationSource(this, 1000)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_manage_boarding, container, false)

        bodyDestinationTextView = view.findViewById(R.id.body_destination)
        bodyPassengerTextView = view.findViewById(R.id.body_passenger)
        nextstation = view.findViewById(R.id.next_station1)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionLauncher()
        setupButtons(view) // 뷰를 전달
        fetchBodyDestination()
        fetchBodyPassengerCount()
        fetnextstationname1()


    }

    private fun permissionLauncher() {
        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                if (granted) startLocationTracking()
            }
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private fun setupButtons(view: View) { // 뷰 파라미터 추가
        val arriveStationLayout: LinearLayout = view.findViewById(R.id.arrive_station)
        arriveStationLayout.setOnClickListener {
            // 출발지와 도착지 변경 로직
            val temp = departureName
            departureName = destinationName
            destinationName = temp

            lifecycleScope.launch(Dispatchers.IO) {
                SupabaseRepository.updateArrive(
                    driverId = driverId,
                    departureName = departureName,
                    destinationName = destinationName
                )
            }

            Toast.makeText(requireContext(), "도착완료", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadStationInfo(departureName: String, destinationName: String, location: LatLng) {
        val driverName = "호날두"
        val speed = 0
        val busPlateNumber = "123가4567"

        lifecycleScope.launch {
            try {
                SupabaseRepository.uploadLocation(
                    driverId = driverId,
                    driverName = driverName,
                    lat = location.latitude,
                    lon = location.longitude,
                    speed = speed,
                    status = "운행중",
                    departureName = departureName,
                    destinationName = destinationName,
                    busPlateNumber = busPlateNumber
                )
                Log.d("DEBUG", "업로드 성공: $destinationName")
            } catch (e: Exception) {
                Log.e("ERROR", "업로드 실패: ${e.message}")
            }
        }
    }

    private fun fetchBodyDestination() {
        lifecycleScope.launch {
            val tinoLocation = SupabaseRepository.bodydestination(driverId)
            tinoLocation?.let {
                bodyDestinationTextView.text = it.departure_name
            } ?: run {
                Log.w("body", "bodydestination에서 데이터를 가져오지 못했습니다.")
                bodyDestinationTextView.text = "도착지 정보 없음"
            }
        }
    }

    private fun fetchBodyPassengerCount() {
        lifecycleScope.launch {
            val passengerNum = SupabaseRepository.fetchPassengerNum()
            passengerNum?.let {
                bodyPassengerTextView.text = "${it}"
            } ?: run {
                bodyPassengerTextView.text = "정보 없음"
            }
        }
    }

    private fun fetnextstationname1() {
        lifecycleScope.launch {
            val next_station = SupabaseRepository.bodydeparture(driverId)
            next_station?.let {
                nextstation.text = it.destination_name
            } ?: run {
                nextstation.text = "정보 없음"
            }
        }
    }

    private fun startLocationTracking() {
        locationService.startLocationUpdates()

        lifecycleScope.launch {
            locationService.getLocationFlow().collectLatest { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)

                    if (::naverMap.isInitialized) {
                        val cameraUpdate =
                            CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude))
                                .animate(CameraAnimation.Easing)
                        naverMap.moveCamera(cameraUpdate)
                    } else {
                        Log.e("LocationTracking", "naverMap is not initialized.")
                    }
                }
            }
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        val initialLocation = LatLng(37.554722, 126.970833)
        val cameraUpdate = CameraUpdate.scrollTo(initialLocation).animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)

        startLocationTracking()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationService.stopLocationUpdates()
    }
}

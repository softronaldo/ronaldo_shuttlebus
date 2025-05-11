package com.example.test_driver2

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainFragment : Fragment(), OnMapReadyCallback {
    private var naverMap: NaverMap? = null // nullable로 변경
    private lateinit var locationSource: FusedLocationSource
    private lateinit var currentSpeedTextView: TextView
    private lateinit var passengerCountTextView: TextView
    private lateinit var mainDestinationTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var mapFragment: MapFragment? = null // 네이버 지도 SDK의 MapFragment 타입

    private val driverId = "3f6941e6-835e-4c62-9f4b-b49617cf312e"
    private var isLocationPermissionGranted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("jehee", "onCreateView() 호출됨")
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("jehee", "onViewCreated() 호출됨")

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        currentSpeedTextView = view.findViewById(R.id.current_speed)
        passengerCountTextView = view.findViewById(R.id.passenger_text)
        mainDestinationTextView = view.findViewById(R.id.main_destination)

        // 위치 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("jehee", "ACCESS_FINE_LOCATION 권한 이미 있음")
            isLocationPermissionGranted = true
            locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
            Log.d("jehee", "FusedLocationSource 초기화됨: $locationSource")
        } else {
            Log.d("jehee", "ACCESS_FINE_LOCATION 권한 없음, 요청 시작")
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            Log.d("jehee", "ActivityCompat.requestPermissions() 호출됨")
        }

        // MapFragment 설정
        val fm = childFragmentManager
        mapFragment = fm.findFragmentById(R.id.mapFragment_main) as? MapFragment
        Log.d("jehee", "findFragmentById(R.id.mapFragment_main) 결과: $mapFragment")
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance().also {
                fm.beginTransaction()
                    .add(R.id.mapFragment_main, it)
                    .commit()
                Log.d("jehee", "새로운 MapFragment 생성 및 추가 트랜잭션 커밋됨: $it")
            }
        }
        mapFragment?.getMapAsync(this) // 안전한 호출 사용
        Log.d("jehee", "mapFragment?.getMapAsync(this) 호출됨")

        fetchSpeedAndUpdateTextView()
        fetchPassengerCountAndUpdateTextView()
        updateMainDestination()

        swipeRefreshLayout.setOnRefreshListener {
            fetchSpeedAndUpdateTextView()
            fetchPassengerCountAndUpdateTextView()
            updateMainDestination()
            swipeRefreshLayout.isRefreshing = false
        }
        Log.d("jehee", "swipeRefreshLayout.setOnRefreshListener() 설정 완료")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("jehee", "위치 권한 허용됨 - onRequestPermissionsResult")
                isLocationPermissionGranted = true
                locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
                naverMap?.let {
                    it.locationSource = locationSource
                    it.locationTrackingMode = LocationTrackingMode.Follow
                    Log.d("jehee", "onRequestPermissionsResult: 지도 설정 적용")
                }
            } else {
                Log.w("jehee", "위치 권한 거부됨 - onRequestPermissionsResult")
                // 권한 거부 시 처리
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("jehee", "onMapReady() 호출됨, naverMap 객체: $naverMap")
        this.naverMap = naverMap

        if (isLocationPermissionGranted) {
            naverMap.locationSource = locationSource
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
            Log.d("jehee", "onMapReady: 위치 권한 있어 설정 완료")
        } else {
            Log.w("jehee", "onMapReady: 위치 권한 없어 설정 보류")
        }
    }

    private fun fetchSpeedAndUpdateTextView() {
        CoroutineScope(Dispatchers.Main).launch {
            val speed = SupabaseRepository.fetchCurrentSpeed(driverId)
            speed?.let {
                currentSpeedTextView.text = "속도:$it km"
            } ?: run {
                currentSpeedTextView.text = "속도: 정보 없음"
            }
            Log.d("jehee", "fetchSpeedAndUpdateTextView() 완료")
        }
    }

    private fun fetchPassengerCountAndUpdateTextView() {
        CoroutineScope(Dispatchers.Main).launch {
            val passengerNum = SupabaseRepository.fetchPassengerNum()
            passengerNum?.let {
                passengerCountTextView.text = "${it}명"
            } ?: run {
                passengerCountTextView.text = "정보\u00A0없음"
            }
            Log.d("jehee", "fetchPassengerCountAndUpdateTextView() 완료")
        }
    }

    private fun updateMainDestination() {
        CoroutineScope(Dispatchers.Main).launch {
            val locationInfo = SupabaseRepository.fetchDepartureAndDestination(driverId)
            locationInfo?.let {
                val displayText = "${it.departure_name}  ->  ${it.destination_name}"
                mainDestinationTextView.text = displayText
            } ?: run {
                mainDestinationTextView.text = "출발지 -> 도착지 정보 없음"
            }
            Log.d("jehee", "updateMainDestination() 완료")
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
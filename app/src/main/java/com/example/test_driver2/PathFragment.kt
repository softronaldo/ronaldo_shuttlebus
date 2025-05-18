package com.example.test_driver2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class PathFragment : Fragment(), OnMapReadyCallback {

    private val client = OkHttpClient()
    private var naverMap: NaverMap? = null
    private val pathOverlayToJeongwang = PathOverlay()
    private val pathOverlayToKoreaTech = PathOverlay()
    private val jeongwangStationMarker = Marker()
    private val koreaTechFrontGateMarker = Marker()
    private lateinit var estimatedTimeTextView: TextView
    private lateinit var leftTimeTextView: TextView
    private lateinit var locationService: LocationService
    private lateinit var locationSource: FusedLocationSource

    private val naverClientId = "baa8zur66x"
    private val naverClientSecret = "6hdqvtnF3u4YKVHSJNbibfkZTFVqJ4WTvgJ4BsrR"

    // 정왕역 좌표
    private val jeongwangStationLatLng = LatLng(37.3515, 126.7427)
    private val jeongwangStationName = "정왕역"

    // 한국공학대학교 정문 좌표
    private val koreaTechFrontGateLatLng = LatLng(37.3393, 126.7329)
    private val koreaTechFrontGateName = "한국공학대학교 정문"

    private val handler = Handler(Looper.getMainLooper())
    private var jeongwangEtaMinutes: Int? = null
    private var koreaTechEtaMinutes: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PathFragment_Lifecycle", "onCreate 호출됨")
        locationService = LocationService(requireContext())
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("PathFragment_Lifecycle", "onCreateView 호출됨")
        val view = inflater.inflate(R.layout.fragment_path, container, false)
        leftTimeTextView = view.findViewById(R.id.left_time)
        estimatedTimeTextView = view.findViewById(R.id.estimated_time)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("PathFragment_Lifecycle", "onViewCreated 호출됨")

        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
                Log.d("PathFragment_Lifecycle", "MapFragment 새로 생성됨")
            }

        Log.d("PathFragment_Lifecycle", "MapFragment: $mapFragment")
        mapFragment.getMapAsync(this)
        Log.d("PathFragment_Lifecycle", "getMapAsync 호출됨")
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        Log.d("PathFragment_Lifecycle", "onMapReady 호출됨")
        this.naverMap = naverMap
        setupMapUiSettings(naverMap)
        addMarkers(naverMap)
        startLocationTracking()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            naverMap.locationSource = locationSource
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
        } else {
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()

        }


    }

    private fun setupMapUiSettings(naverMap: NaverMap) {
        val uiSettings = naverMap.uiSettings
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isRotateGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        // uiSettings.isMyLocationButtonEnabled = true
    }

    private fun addMarkers(naverMap: NaverMap) {
        jeongwangStationMarker.position = jeongwangStationLatLng
        jeongwangStationMarker.captionText = jeongwangStationName
        jeongwangStationMarker.width = 50
        jeongwangStationMarker.height = 70
        jeongwangStationMarker.map = naverMap

        koreaTechFrontGateMarker.position = koreaTechFrontGateLatLng
        koreaTechFrontGateMarker.captionText = koreaTechFrontGateName
        koreaTechFrontGateMarker.width = 50
        koreaTechFrontGateMarker.height = 70
        koreaTechFrontGateMarker.map = naverMap
    }

    private fun startLocationTracking() {
        locationService.startLocationUpdates()

        lifecycleScope.launch {
            locationService.getLocationFlow().collectLatest { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    Log.d("Location", "Current Location (Flow): $currentLatLng")
                    updateMapCamera(currentLatLng)
                    drawRouteAndGetTime(currentLatLng)
                    calculateAndDisplayEta(currentLatLng)
                }
            }
        }
    }

    private fun updateMapCamera(latLng: LatLng) {
        val cameraUpdate = CameraUpdate.scrollTo(latLng)
        naverMap?.moveCamera(cameraUpdate)
    }

    private fun drawRouteAndGetTime(currentLatLng: LatLng) {
        // 현재 위치 -> 정왕역 경로 요청
        requestDirection(currentLatLng, jeongwangStationLatLng, pathOverlayToJeongwang, "정왕역")

        // 현재 위치 -> 한국공학대학교 정문 경로 요청
        requestDirection(currentLatLng, koreaTechFrontGateLatLng, pathOverlayToKoreaTech, "한국공학대학교 정문")
    }

    private fun requestDirection(
        startLatLng: LatLng,
        goalLatLng: LatLng,
        pathOverlay: PathOverlay,
        destinationName: String
    ) {
        val url = "https://maps.apigw.ntruss.com/map-direction/v1/driving?" +
                "start=${startLatLng.longitude},${startLatLng.latitude}&" +
                "goal=${goalLatLng.longitude},${goalLatLng.latitude}&" +
                "option=trafast"

        val request = Request.Builder()
            .url(url)
            .header("X-NCP-APIGW-API-KEY-ID", naverClientId)
            .header("X-NCP-APIGW-API-KEY", naverClientSecret)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Direction", "$destinationName API 호출 실패: ${e.message}")
                activity?.runOnUiThread {
                    pathOverlay.map = null
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("Direction", "$destinationName API 응답: $body")
                if (response.isSuccessful && body != null) {
                    try {
                        val jsonResponse = JSONObject(body)
                        val route = jsonResponse.getJSONObject("route")
                        val trafast = route.getJSONArray("trafast")
                        if (trafast.length() > 0) {
                            val path = trafast.getJSONObject(0).getJSONArray("path")
                            val coords = mutableListOf<LatLng>()

                            for (i in 0 until path.length()) {
                                val point = path.getJSONArray(i)
                                val lon = point.getDouble(0)
                                val lat = point.getDouble(1)
                                coords.add(LatLng(lat, lon))
                            }

                            activity?.runOnUiThread {
                                pathOverlay.coords = coords
                                pathOverlay.color = if (destinationName == "정왕역") Color.BLUE else Color.GREEN
                                pathOverlay.width = 15
                                pathOverlay.map = naverMap
                            }
                        } else {
                            Log.w("Direction", "$destinationName trafast 배열이 비어 있습니다.")
                            activity?.runOnUiThread {
                                pathOverlay.map = null
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("Direction", "$destinationName JSON 파싱 에러: ${e.message}")
                        activity?.runOnUiThread {
                            pathOverlay.map = null
                        }
                    }
                } else {
                    Log.e("Direction", "$destinationName API 응답 실패: ${response.code} - $body")
                    activity?.runOnUiThread {
                        pathOverlay.map = null
                    }
                }
            }
        })
    }

    private suspend fun getCurrentSpeed(): Int {
        val speed = SupabaseRepository.fetchCurrentSpeed("3f6941e6-835e-4c62-9f4b-b49617cf312e")
        Log.d("Speed", "Current Speed from Supabase: $speed")
        return speed ?: 0
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(start.latitude)) *
                cos(Math.toRadians(end.latitude)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private suspend fun calculateAndDisplayEta(currentLatLng: LatLng) {
        val currentSpeedKph = getCurrentSpeed()
        val currentSpeedMps = currentSpeedKph.toDouble() / 3.6

        // 정왕역
        val distanceToJeongwang = calculateDistance(currentLatLng, jeongwangStationLatLng)
        jeongwangEtaMinutes = if (currentSpeedMps > 0) (distanceToJeongwang / currentSpeedMps / 60).roundToInt() else 0

        // 한국공학대학교 정문
        val distanceToKoreaTech = calculateDistance(currentLatLng, koreaTechFrontGateLatLng)
        koreaTechEtaMinutes = if (currentSpeedMps > 0) (distanceToKoreaTech / currentSpeedMps / 60).roundToInt() else 0

        updateEtaTextView()
    }

    private fun updateEtaTextView() {
        val jeongwangEtaText = jeongwangEtaMinutes?.let { "$it 분" } ?: "정보 없음"
        val koreaTechEtaText = koreaTechEtaMinutes?.let { "$it 분" } ?: "정보 없음"

        leftTimeTextView.text = "(정왕역: $jeongwangEtaText / \n 한국공학대학교: $koreaTechEtaText)"


        val currentTime = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val jeongwangArrivalTimeCalendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, jeongwangEtaMinutes ?: 0)
        }
        val jeongwangArrivalTime = if (jeongwangEtaMinutes != null) timeFormat.format(jeongwangArrivalTimeCalendar.time) else "정보 없음"

        val koreaTechArrivalTimeCalendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, koreaTechEtaMinutes ?: 0)
        }
        val koreaTechArrivalTime = if (koreaTechEtaMinutes != null) timeFormat.format(koreaTechArrivalTimeCalendar.time) else "정보 없음"

        estimatedTimeTextView.text = "(정왕역: $jeongwangArrivalTime / \n 한국공학대학교: $koreaTechArrivalTime)"
    }


    override fun onStop() {
        super.onStop()
        locationService.stopLocationUpdates()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
package com.example.test_driver2

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PathFragment : Fragment(), OnMapReadyCallback {

    private val client = OkHttpClient()
    private var naverMap: NaverMap? = null
    private val pathOverlay = PathOverlay()
    private val startMarker = Marker()
    private val endMarker = Marker()

    // **여기에 본인의 Client ID와 Client Secret을 정확하게 입력하세요!**
    private val naverClientId = "baa8zur66x"
    private val naverClientSecret = "6hdqvtnF3u4YKVHSJNbibfkZTFVqJ4WTvgJ4BsrR"

    // 출발지와 도착지 좌표 (실제 사용할 좌표로 변경하세요!)
    private val startLatLng = LatLng(37.35782, 126.73453) // 한국공학대학교
    private val endLatLng = LatLng(37.34151, 126.73248)   // 정왕역

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_path, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        setupMapUiSettings(naverMap)
        drawRouteBetweenStops(naverMap)
        addMarkers(naverMap)
        moveCameraToStartPosition(naverMap)
    }

    private fun setupMapUiSettings(naverMap: NaverMap) {
        val uiSettings = naverMap.uiSettings
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isRotateGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
    }

    private fun moveCameraToStartPosition(naverMap: NaverMap) {
        val cameraUpdate = CameraUpdate.scrollTo(startLatLng)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun addMarkers(naverMap: NaverMap) {
        startMarker.position = startLatLng
        startMarker.captionText = "출발지"
        startMarker.map = naverMap

        endMarker.position = endLatLng
        endMarker.captionText = "도착지"
        endMarker.map = naverMap
    }

    private fun drawRouteBetweenStops(naverMap: NaverMap) {
        val url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?" +
                "start=${startLatLng.longitude},${startLatLng.latitude}&" +
                "goal=${endLatLng.longitude},${endLatLng.latitude}&" +
                "option=trafast"

        val request = Request.Builder()
            .url(url)
            .header("X-NCP-APIGW-API-KEY-ID", naverClientId)
            .header("X-NCP-APIGW-API-KEY", naverClientSecret)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Direction5", "API 호출 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("Direction5", "API 응답: $body")
                if (response.isSuccessful && body != null) {
                    try {
                        val jsonResponse = JSONObject(body)
                        val route = jsonResponse.getJSONObject("route")
                        val traoptimal = route.getJSONArray("traoptimal")
                        val path = traoptimal.getJSONObject(0).getJSONArray("path")
                        val coords = mutableListOf<LatLng>()

                        for (i in 0 until path.length()) {
                            val point = path.getJSONArray(i)
                            val lon = point.getDouble(0)
                            val lat = point.getDouble(1)
                            coords.add(LatLng(lat, lon))
                        }

                        activity?.runOnUiThread {
                            pathOverlay.coords = coords
                            pathOverlay.color = Color.BLUE
                            pathOverlay.width = 10
                            pathOverlay.map = naverMap
                        }

                    } catch (e: Exception) {
                        Log.e("Direction5", "JSON 파싱 에러: ${e.message}")
                    }
                } else {
                    Log.e("Direction5", "API 응답 실패: ${response.code} - $body")
                }
            }
        })
    }
}
package com.example.test_driver2

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay

class PathFragment : Fragment(), OnMapReadyCallback {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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
        drawRouteBetweenStops(naverMap)
    }

    private fun drawRouteBetweenStops(naverMap: NaverMap) {
        // 한국공학대학교 정문 버스 정류장 좌표 (실제 좌표로 변경해야 합니다!)
        val startLatLng = LatLng(37.35782, 126.73453)

        // 정왕역 환승센터 정류장 좌표 (실제 좌표로 변경해야 합니다!)
        val endLatLng = LatLng(37.34151, 126.73248)

        // 경로 그리기
        val path = PathOverlay()
        path.coords = listOf(startLatLng, endLatLng)
        path.color = Color.BLUE
        path.width = 10
        path.map = naverMap

        // 출발지 마커
        val startMarker = Marker()
        startMarker.position = startLatLng
        startMarker.captionText = "한국공학대학교"
        startMarker.map = naverMap

        // 도착지 마커
        val endMarker = Marker()
        endMarker.position = endLatLng
        endMarker.captionText = "정왕역"
        endMarker.map = naverMap

        // 카메라 이동 (선택 사항 - 경로가 잘 보이도록)
        val cameraUpdate = com.naver.maps.map.CameraUpdate.scrollTo(startLatLng)
        naverMap.moveCamera(cameraUpdate)
    }
}
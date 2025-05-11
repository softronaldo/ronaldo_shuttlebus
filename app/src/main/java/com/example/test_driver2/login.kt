package com.example.test_driver2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.test_driver2.databinding.ActivityLoginBinding // 바뀐 부분
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject //


class Login : AppCompatActivity() {

    private lateinit var locationService: LocationService
    private val driverId = "3f6941e6-835e-4c62-9f4b-b49617cf312e"
    private lateinit var realtimeClient: SupabaseRealtimeClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("jehee", "Login 액티비티 onCreate 호출됨") // onCreate 시작 로그

        val binding = ActivityLoginBinding.inflate(layoutInflater) // 바뀐 부분
        setContentView(binding.root)
        enableEdgeToEdge()

        Log.d("jehee", "바인딩 및 ContentView 설정 완료")

        locationService = LocationService(this)
        Log.d("jehee", "LocationService 초기화 완료")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginactivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d("jehee", "WindowInsetsListener 설정 완료")

        val id = "ronaldo123"
        val password = "1234"
        Log.d("jehee", "기본 아이디/비밀번호 설정 완료")


        binding.logInButton.setOnClickListener {
            Log.d("jehee", "로그인 버튼 클릭됨") // 로그인 버튼 클릭 로그
            val enteredId = binding.idText.text.toString()
            val enteredPw = binding.pwText.text.toString()
            Log.d("jehee", "입력된 아이디: $enteredId, 비밀번호: $enteredPw")

            if (true) {
//            if (enteredId == id && enteredPw == password) {
                Log.d("jehee", "로그인 성공 조건 충족")
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("departureName", "한국공학대학교 정문")
                intent.putExtra("destinationName", "정왕역")
                Log.d("jehee", "Intent 생성, MainFragment로 이동 시도")
                startActivity(intent)
                Log.d("jehee", "startActivity 호출됨")
                Toast.makeText(this, "로그인 완료! 운행시작", Toast.LENGTH_SHORT).show()
                Log.d("jehee", "토스트 메시지 표시됨")

                startRealtimeConnection()
                Log.d("jehee", "startRealtimeConnection 호출됨")
                startLocationTracking()
                Log.d("jehee", "startLocationTracking 호출됨")

            } else {
                Log.d("jehee", "로그인 실패 조건")
                Toast.makeText(this, "ID/PW를 다시 입력해주세요", Toast.LENGTH_SHORT).show()
                Log.d("jehee", "로그인 실패 토스트 메시지 표시됨")
            }
        }
        Log.d("jehee", "로그인 버튼 리스너 설정 완료")
    }
    private fun startRealtimeConnection() {
        Log.d("jehee", "startRealtimeConnection() 호출됨")
        val url = "https://yxahehaxkearsllzzcxp.supabase.co"
        val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl4YWhlaGF4a2VhcnNsbHp6Y3hwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMzMzQzMDAsImV4cCI6MjA1ODkxMDMwMH0.ZwmX86jazv_ynCzkpunHALd5zYjiMpkxNfdvg6mgdyQ"

        realtimeClient = SupabaseRealtimeClient(url, anonKey)
        Log.d("jehee", "SupabaseRealtimeClient 생성됨")
        realtimeClient.connect()
        Log.d("jehee", "realtimeClient.connect() 호출됨")
    }

    private fun startLocationTracking() {
        Log.d("jehee", "startLocationTracking() 호출됨")
        if (::locationService.isInitialized) {
            Log.d("jehee", "locationService 초기화 확인됨")
            locationService.startLocationUpdates()
            Log.d("jehee", "locationService.startLocationUpdates() 호출됨")

            lifecycleScope.launch {
                Log.d("jehee", "위치 정보 수집 코루틴 시작")
                locationService.getLocationFlow().collectLatest { location ->
                    Log.d("jehee", "getLocationFlow() collect: $location")
                    location?.let {
                        val speed = (it.speed * 3.6).toInt()
                        Log.d("jehee", "현재 속도: $speed")

                        Log.d("jehee", "Supabase DB 업데이트 시작")
                        // ✅ Supabase DB에 업로드
                        SupabaseRepository.updateLocation(
                            driverId = driverId,
                            lat = it.latitude,
                            lon = it.longitude,
                            speed = speed,
                        )
                        Log.d("jehee", "Supabase DB 업데이트 완료")

//                        // ✅ WebSocket broadcast 전송
//                        val posData = JSONObject().apply {
//                            put("driverId", driverId)
//                            put("lat", it.latitude)
//                            put("lon", it.longitude)
//                            put("speed", speed)
//                            put("status", status)
//                        }
//                        Log.d("jehee", "WebSocket broadcast 시작")
//                        realtimeClient.sendPositionBroadcast(posData)
//                        Log.d("jehee", "WebSocket broadcast 완료")
                    } ?: Log.d("jehee", "위치 정보가 null임")
                }
            }
            Log.d("jehee", "위치 정보 수집 코루틴 설정 완료")
        } else {
            Log.e("jehee", "LocationService가 초기화되지 않음")
            Toast.makeText(this, "LocationService가 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }
        Log.d("jehee", "startLocationTracking() 종료")
    }
}


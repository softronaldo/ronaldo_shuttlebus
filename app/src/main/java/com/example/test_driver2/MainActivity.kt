package com.example.test_driver2


import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test_driver2.databinding.ActivityMainBinding
import com.example.test_driver2.ManageBoardingFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("JEHEE", "MainActivity onCreate() 시작")

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            Log.d("JEHEE", "ActivityMainBinding inflate 완료")
            enableEdgeToEdge()
            Log.d("JEHEE", "enableEdgeToEdge() 호출 완료")
            setContentView(binding.root)
            Log.d("JEHEE", "setContentView() 호출 완료")
            setSupportActionBar(binding.toolbar)
            Log.d("JEHEE", "setSupportActionBar() 호출 완료, toolbar: ${binding.toolbar}")

            // "School Shuttle Driver"에 대한 SpannableString 생성 (onCreate에서 설정)
            val titleText = "School Shuttle Driver"
            val spannableString = SpannableString(titleText)
            val color = ContextCompat.getColor(this, R.color.blue) // res/values/colors.xml에 정의된 색상 사용
            spannableString.setSpan(
                ForegroundColorSpan(color),
                0,
                titleText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD), // 굵게 스타일 적용
                0,
                titleText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            title = spannableString
            Log.d("JEHEE", "title 설정 완료: $title")

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                Log.d("JEHEE", "setOnApplyWindowInsetsListener 호출됨")
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                Log.d("JEHEE", "패딩 설정됨: left=${systemBars.left}, top=${systemBars.top}, right=${systemBars.right}, bottom=${systemBars.bottom}")
                insets
            }
            Log.d("JEHEE", "setOnApplyWindowInsetsListener 설정 완료")

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                Log.d("JEHEE", "bottomNavigation.setOnItemSelectedListener 호출됨, item ID: ${item.itemId}")
                when (item.itemId) {
                    R.id.menu_first -> {
                        // "School Shuttle Driver"에 색상 및 굵게 스타일 적용
                        val titleText = "School Shuttle Driver"
                        val spannableString = SpannableString(titleText)
                        val color = ContextCompat.getColor(this@MainActivity, R.color.blue)
                        spannableString.setSpan(
                            ForegroundColorSpan(color),
                            0,
                            titleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannableString.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            titleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setTitle(spannableString)
                        val fragment = MainFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, fragment).commit()
                        Log.d("JEHEE", "menu_first 선택됨, MainFragment로 교체")
                        return@setOnItemSelectedListener true
                    }
                    R.id.menu_second -> {
                        // "운행 경로"에 색상 및 굵게 스타일 적용
                        val titleText = "운행 경로"
                        val spannableString = SpannableString(titleText)
                        val color = ContextCompat.getColor(this@MainActivity, R.color.blue)  // 동일한 색상 사용
                        spannableString.setSpan(
                            ForegroundColorSpan(color),
                            0,
                            titleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

                        )
                        spannableString.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            titleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setTitle(spannableString)
                        val fragment = PathFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, fragment).commit()
                        Log.d("JEHEE", "menu_second 선택됨, PathFragment로 교체")
                        return@setOnItemSelectedListener true
                    }
                    R.id.menu_third -> {
                        // "운행 기록"에 색상 및 굵게 스타일 적용
                        val titleText = "운행 기록"
                        val spannableString = SpannableString(titleText)
                        val color = ContextCompat.getColor(this@MainActivity, R.color.blue)  // 동일한 색상 사용
                        spannableString.setSpan(
                            ForegroundColorSpan(color),
                            0,
                            titleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannableString.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            titleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setTitle(spannableString)
                        val fragment = HistoryFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, fragment).commit()
                        Log.d("JEHEE", "menu_third 선택됨, HistoryFragment로 교체")
                        return@setOnItemSelectedListener true
                    }
                    R.id.menu_fourth -> {
                        // "탑승자 관리"에 색상 및 굵게 스타일 적용
                        val titleText = "탑승자 관리"
                        val spannableString = SpannableString(titleText)
                        val color = ContextCompat.getColor(this@MainActivity, R.color.blue)  // 동일한 색상 사용
                        spannableString.setSpan(
                            ForegroundColorSpan(color),
                            0,
                            titleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannableString.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            titleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setTitle(spannableString)
                        val fragment = ManageBoardingFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, fragment).commit()
                        Log.d("JEHEE", "menu_fourth 선택됨, ManageBoardingFragment로 교체")
                        return@setOnItemSelectedListener true
                    }
                    else -> {
                        Log.d("JEHEE", "알 수 없는 메뉴 아이템 선택됨: ${item.itemId}")
                        return@setOnItemSelectedListener true
                    }
                }
            }
            Log.d("JEHEE", "bottomNavigation.setOnItemSelectedListener 설정 완료")

        } catch (e: Exception) {
            Log.e("JEHEE", "onCreate() 중 예외 발생: ${e.message}", e)
        }

        Log.d("JEHEE", "MainActivity onCreate() 종료")
    }
}

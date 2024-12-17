package androidtown.org.moveon;

import android.os.Bundle
import android.widget.Toast
import androidtown.org.moveon.api.ApiService
import androidtown.org.moveon.api.RetrofitClient
import androidtown.org.moveon.api.UserStepInfo
import androidtown.org.moveon.api.UserStepRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidtown.org.moveon.map.MapFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val ApiService = RetrofitClient.instance.create(ApiService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) { // 중복 추가 방지
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RunningFragment()) // MapFragment를 기본 Fragment로 설정
                .commit()
        }
        // Set listener for navigation item selection
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            var selectedFragment: Fragment? = null
            when (menuItem.itemId) {
                R.id.nav_running -> selectedFragment = RunningFragment()
                R.id.nav_challenge -> selectedFragment = ChallengeFragment()
                R.id.nav_ranking -> selectedFragment = RankingFragment()
                R.id.nav_mypage -> selectedFragment = MyFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }
            true
        }
        sendStepData()
    }
    private fun sendStepData() {
        // 보낼 데이터
        val userStepInfo = UserStepInfo(
            date = "2024-12-17",
            steps = 1557,
            distance = 50,
            run_time = "00:12:23",
            memo = "오늘 하루 오운완",
            userId = 1,
            hrate = 0
        )

        val request = UserStepRequest(
            userStepInfo = userStepInfo,
        )

        // 서버 요청
        ApiService.uploadUserSteps(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "데이터 전송 성공!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

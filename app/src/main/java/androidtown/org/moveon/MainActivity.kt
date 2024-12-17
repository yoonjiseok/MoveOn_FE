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
    }

}

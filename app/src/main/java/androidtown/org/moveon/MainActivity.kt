package androidtown.org.moveon;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, RunningFragment())
            .commit()

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

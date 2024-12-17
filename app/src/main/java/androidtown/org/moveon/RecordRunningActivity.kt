package androidtown.org.moveon

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidtown.org.moveon.R
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class RecordRunningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_running)

        // Get the timer time, distance, steps, and calories from the Intent
        val runningTimeInSeconds = intent.getIntExtra("RUNNING_TIME", 0)
        val totalDistance = intent.getFloatExtra("TOTAL_DISTANCE", 0f)
        val totalSteps = intent.getIntExtra("TOTAL_STEPS", 0)
        val totalPixel = intent.getIntExtra("TOTAL_PIXEL", 0)
        val mapImageUrl = intent.getStringExtra("MAP_IMAGE_URL") // 지도 URL 받기
        val caloriesBurned: Int = intent.getIntExtra("CALORIES_BURNED", 0)

        // Find and update the "달린 시간" TextView
        val timeValueText: TextView = findViewById(R.id.timeValue)
        timeValueText.text = formatTime(runningTimeInSeconds)

        // Find and update the "달린 거리" TextView
        val distanceValueText: TextView = findViewById(R.id.distance_value)
        distanceValueText.text = formatDistance(totalDistance)

        // Find and update the "걸음 수" TextView
        val stepValueText: TextView = findViewById(R.id.step_count)
        stepValueText.text = formatSteps(totalSteps)

        // Find and update the "칼로리 소모량" TextView
        val caloriesValueText: TextView = findViewById(R.id.calories)
        caloriesValueText.text = formatCalories(caloriesBurned)

        // Find and update the "현재 칸의 수" TextView
        val currentPixelText: TextView = findViewById(R.id.today_count_number)
        currentPixelText.text = formatSteps(totalPixel)

        // Load the map image using Glide
        val mapView: ImageView = findViewById(R.id.map_view)
        if (mapImageUrl != null) {
            Glide.with(this)
                .load(mapImageUrl)
                .into(mapView)
        }

        // Handle back navigation
        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    private fun formatDistance(distance: Float): String {
        return String.format("%.2f m", distance) // Format distance to 2 decimal places
    }

    private fun formatSteps(steps: Int): String {
        return "$steps" // Format steps as a simple string
    }

    private fun formatCalories(calories: Int): String {
        return "$calories kcal"
    }

}

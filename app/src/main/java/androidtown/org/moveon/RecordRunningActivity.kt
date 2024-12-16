package androidtown.org.moveon

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecordRunningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_running)

        // Get the timer time, distance, and steps from the Intent
        val runningTimeInSeconds = intent.getIntExtra("RUNNING_TIME", 0)
        val totalDistance = intent.getFloatExtra("TOTAL_DISTANCE", 0f)
        val totalSteps = intent.getIntExtra("TOTAL_STEPS", 0)

        // Find and update the "달린 시간" TextView
        val timeValueText: TextView = findViewById(R.id.timeValue)
        timeValueText.text = formatTime(runningTimeInSeconds)

        // Find and update the "달린 거리" TextView
        val distanceValueText: TextView = findViewById(R.id.distance_value)
        distanceValueText.text = formatDistance(totalDistance)

        // Find and update the "걸음 수" TextView
        val stepValueText: TextView = findViewById(R.id.step_count)
        stepValueText.text = formatSteps(totalSteps)

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
}

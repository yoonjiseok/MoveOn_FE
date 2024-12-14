package androidtown.org.moveon

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecordRunningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_running)

        // Get the timer time from the Intent
        val runningTimeInSeconds = intent.getIntExtra("RUNNING_TIME", 0)

        // Find and update the "달린 시간" TextView
        val timeValueText: TextView = findViewById(R.id.timeValue)
        timeValueText.text = formatTime(runningTimeInSeconds)

        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressed() // Handle back navigation
        }
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }
}

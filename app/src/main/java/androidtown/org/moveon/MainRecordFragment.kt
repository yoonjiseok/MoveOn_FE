package androidtown.org.moveon

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class MainRecordFragment : Fragment(R.layout.fragment_main_record) {

    private var isTimerRunning = false
    private var timeInSeconds = 0
    private lateinit var handler: Handler

    private lateinit var timeValueText: TextView
    private lateinit var playButton: ImageView
    private lateinit var pauseButton: ImageView
    private lateinit var stopButton: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        timeValueText = view.findViewById(R.id.timeValue)
        playButton = view.findViewById(R.id.play_button)
        pauseButton = view.findViewById(R.id.pause_button)
        stopButton = view.findViewById(R.id.stop_button)

        // Initialize button visibility
        pauseButton.visibility = View.GONE
        stopButton.visibility = View.GONE

        handler = Handler(Looper.getMainLooper())

        // Play button click listener
        playButton.setOnClickListener {
            startTimer()
            playButton.visibility = View.GONE // Hide play button
            pauseButton.visibility = View.VISIBLE // Show pause button
            stopButton.visibility = View.VISIBLE // Show stop button
        }

        // Pause button click listener
        pauseButton.setOnClickListener {
            pauseTimer()
            pauseButton.visibility = View.GONE // Hide pause button
            playButton.visibility = View.VISIBLE // Show play button
        }

        // Stop button click listener
        stopButton.setOnClickListener {
            val finalTime = timeInSeconds // Capture the final timer value
            stopTimer()
            pauseButton.visibility = View.GONE // Hide pause button
            playButton.visibility = View.VISIBLE // Show play button
            stopButton.visibility = View.GONE // Hide stop button

            // Navigate to RecordRunningActivity with the final time
            navigateToRecordRunningActivity(finalTime)
        }
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            handler.post(timerRunnable)
        }
    }

    private fun pauseTimer() {
        isTimerRunning = false
        handler.removeCallbacks(timerRunnable)
    }

    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacks(timerRunnable)
        updateTimerText() // Ensure final time is displayed
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                timeInSeconds++
                updateTimerText()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun updateTimerText() {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        val hours = timeInSeconds / 3600
        timeValueText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun navigateToRecordRunningActivity(finalTime: Int) {
        try {
            val intent = Intent(requireContext(), RecordRunningActivity::class.java)
            intent.putExtra("RUNNING_TIME", finalTime) // Pass the timer time
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

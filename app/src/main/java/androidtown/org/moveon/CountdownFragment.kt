package androidtown.org.moveon

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment

class CountdownFragment : Fragment() {

    private lateinit var countdownText: TextView
    private lateinit var circleProgress: ProgressBar
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var progressAnimator: ValueAnimator
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_countdown, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countdownText = view.findViewById(R.id.countdownText)
        circleProgress = view.findViewById(R.id.circleProgress)
        startCountdown()
    }

    private fun startCountdown() {
        val totalDuration = 3500L  // 3초 + "GO!" 0.5초
        val animationDuration = 1000L // 1초 동안 프로그레스바 채우기

        countDownTimer = object : CountDownTimer(totalDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()

                if (secondsRemaining > 0) {
                    countdownText.text = secondsRemaining.toString()
                    animateProgress(0, 110, animationDuration)
                } else {
                    // "GO!" 상태에서는 애니메이션 없이 100% 유지
                    countdownText.text = "GO!"
                    circleProgress.progress = 100  // 원이 유지되도록 설정

                    // "GO!"가 0.3초 동안 유지된 후 화면 전환
                    handler.postDelayed({
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, MainRecordFragment())
                            .commit()
                    }, 300)
                }
            }

            override fun onFinish() {
                // "GO!" 후 0.2초 동안 유지되므로 별도 처리 X
            }
        }

        countDownTimer.start()
        circleProgress.progress = 0
    }

    private fun animateProgress(start: Int, end: Int, duration: Long) {
        progressAnimator = ValueAnimator.ofInt(start, end).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                circleProgress.progress = animation.animatedValue as Int
            }
            start()
        }
    }
}
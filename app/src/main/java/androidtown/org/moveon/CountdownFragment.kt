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
        // 전체 카운트다운 시간 (3초 + GO! 1초)
        val totalDuration = 4000L
        val animationDuration = 800L // 애니메이션 시간
        
        countDownTimer = object : CountDownTimer(totalDuration, 1000) {
            var previousNumber = 3

            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                
                if (secondsRemaining > 0) {
                    countdownText.text = secondsRemaining.toString()
                    
                    // 이전 애니메이터 취소
                    if (::progressAnimator.isInitialized) {
                        progressAnimator.cancel()
                    }

                    when {
                        // 3에서 2로 전환
                        previousNumber == 3 && secondsRemaining == 2 -> {
                            animateProgress(0, 50, animationDuration) // 9시에서 3시까지
                        }
                        // 2에서 1로 전환
                        previousNumber == 2 && secondsRemaining == 1 -> {
                            animateProgress(50, 100, animationDuration) // 3시에서 한바퀴 완성
                        }
                        // 처음 시작할 때 (3 표시)
                        secondsRemaining == 3 -> {
                            circleProgress.progress = 0 // 9시 위치에서 시작
                        }
                    }
                    
                    previousNumber = secondsRemaining
                } else {
                    countdownText.text = "GO!"
                }
            }

            override fun onFinish() {
                // Navigate to MainRecordFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MainRecordFragment())
                    .commit()
            }
        }

        // 카운트다운 시작
        countDownTimer.start()
        // 초기 프로그레스 설정
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

    override fun onDestroyView() {
        super.onDestroyView()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        if (::progressAnimator.isInitialized) {
            progressAnimator.cancel()
        }
        handler.removeCallbacksAndMessages(null)
    }
}
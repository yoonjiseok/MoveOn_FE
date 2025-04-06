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
    private var lastNumber = 0

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
        
        // 초기화
        circleProgress.progress = 0
        lastNumber = 0
        countdownText.text = "1"
        
        startCountdown()
    }

    private fun startCountdown() {
        val totalDuration = 3000L  // 정확히 3초
        val animationDuration = 1000L // 1초 동안 프로그레스바 채우기

        // 시작할 때 확실하게 초기화
        circleProgress.progress = 0
        lastNumber = 0

        countDownTimer = object : CountDownTimer(totalDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsElapsed = ((totalDuration - millisUntilFinished) / 1000).toInt()
                val currentNumber = when(secondsElapsed) {
                    0 -> 1
                    1 -> 2
                    else -> 3
                }
                
                when {
                    currentNumber == 1 && lastNumber == 0 -> {
                        // 초기 상태: 0%
                        circleProgress.progress = 0
                    }
                    currentNumber == 2 && lastNumber == 1 -> {
                        // 1->2 전환: 0% -> 50% 애니메이션
                        animateProgress(0, 50, animationDuration)
                    }
                    currentNumber == 3 && lastNumber == 2 -> {
                        // 2->3 전환: 50% -> 100% 애니메이션
                        animateProgress(50, 100, animationDuration)
                    }
                }
                
                countdownText.text = currentNumber.toString()
                lastNumber = currentNumber
            }

            override fun onFinish() {
                // 실행 중인 애니메이션 취소
                if (::progressAnimator.isInitialized) {
                    progressAnimator.cancel()
                }
                
                // 상태 초기화
                circleProgress.progress = 0
                lastNumber = 0
                
                handler.postDelayed({
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MainRecordFragment())
                        .commit()
                }, 200)
            }
        }

        countDownTimer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        if (::progressAnimator.isInitialized) {
            progressAnimator.cancel()
        }
        // 파괴될 때도 초기화
        circleProgress.progress = 0
        lastNumber = 0
    }

    private fun animateProgress(start: Int, end: Int, duration: Long) {
        if (::progressAnimator.isInitialized) {
            progressAnimator.cancel()
        }
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
package androidtown.org.moveon;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidtown.org.moveon.R
import androidx.fragment.app.Fragment

class ChallengeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_challenge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 두 버튼 참조
        val btnGame = view.findViewById<LinearLayout>(R.id.btnGame)
        val btnGame2 = view.findViewById<LinearLayout>(R.id.btnGame2)

        // 배경 리소스 설정

        var btnGameBackground = R.drawable.challenge_background_green
        var btnGame2Background = R.drawable.challenge_background_gray

        // 클릭 이벤트: btnGame 클릭 시
        btnGame.setOnClickListener {
            // 이미 초록색 배경이면 무시
            if (btnGameBackground == R.drawable.challenge_background_gray){
                btnGame.setBackgroundResource(btnGame2Background)
                btnGame2.setBackgroundResource(btnGameBackground)

                // 리소스 교체
                val temp = btnGameBackground
                btnGameBackground = btnGame2Background
                btnGame2Background = temp
            }

        }

        btnGame2.setOnClickListener {
            // 이미 초록색 배경이면 무시
            if (btnGame2Background == R.drawable.challenge_background_gray){
                btnGame.setBackgroundResource(btnGame2Background)
                btnGame2.setBackgroundResource(btnGameBackground)

                // 리소스 교체
                val temp = btnGameBackground
                btnGameBackground = btnGame2Background
                btnGame2Background = temp
            }

        }
    }
}

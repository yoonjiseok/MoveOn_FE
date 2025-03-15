package androidtown.org.moveon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidtown.org.moveon.repository.MarathonBoardFragment

class ChallengeFragment : Fragment() {

    private var selectedChallenge: String? = null // 현재 선택된 챌린지 (null, "game", "marathon")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_challenge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGame = view.findViewById<LinearLayout>(R.id.btnGame) // 땅따먹기 버튼
        val btnGame2 = view.findViewById<LinearLayout>(R.id.btnGame2) // 미니 마라톤 버튼
        val btnComplete = view.findViewById<TextView>(R.id.btnComplete) // 완료 버튼

        btnGame.setOnClickListener {
            if (selectedChallenge != "game") {
                btnGame.setBackgroundResource(R.drawable.challenge_background_green) // 땅따먹기 → 초록색
                btnGame2.setBackgroundResource(R.drawable.challenge_background_gray) // 미니 마라톤 → 회색
                selectedChallenge = "game"
            } else {
                btnGame.setBackgroundResource(R.drawable.challenge_background_gray) // 땅따먹기 → 회색
                selectedChallenge = null
            }
        }

        btnGame2.setOnClickListener {
            if (selectedChallenge != "marathon") {
                btnGame2.setBackgroundResource(R.drawable.challenge_background_green) // 미니 마라톤 → 초록색
                btnGame.setBackgroundResource(R.drawable.challenge_background_gray) // 땅따먹기 → 회색
                selectedChallenge = "marathon"
            } else {
                btnGame2.setBackgroundResource(R.drawable.challenge_background_gray) // 미니 마라톤 → 회색
                selectedChallenge = null
            }
        }

        btnComplete.setOnClickListener {
            if (selectedChallenge == "marathon") {
                // 완료 버튼 클릭 시 `MarathonBoardFragment`로 이동
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MarathonBoardFragment())
                    .addToBackStack(null) // 뒤로 가기 버튼을 눌렀을 때 이전 화면으로 돌아갈 수 있도록 설정
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
        }
    }
}

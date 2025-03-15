package androidtown.org.moveon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class MarathonInformationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_marathon_information, container, false)

        // UI 요소 연결
        val titleTextView: TextView = view.findViewById(R.id.marathonTitle)
        val locationTextView: TextView = view.findViewById(R.id.textLocation)
        val distanceTextView: TextView = view.findViewById(R.id.textDistance)
        val rewardTextView: TextView = view.findViewById(R.id.textReward)
        val startLocationTextView: TextView = view.findViewById(R.id.textStartLocation)
        val periodTextView: TextView = view.findViewById(R.id.textPeriod)
        val countTextView: TextView = view.findViewById(R.id.textcount)
        val notice1TextView: TextView = view.findViewById(R.id.textNotice1)
        val notice2TextView: TextView = view.findViewById(R.id.textNotice2)
        val challengeButton: Button = view.findViewById(R.id.btnChallenge)

        // 데이터 설정 (arguments를 통한 데이터 전달)
        arguments?.let {
            titleTextView.text = it.getString("title", "여의도 한강 공원")
            locationTextView.text = it.getString("location", "서울")
            distanceTextView.text = it.getString("distance", "10km")
            rewardTextView.text = it.getString("reward", "500p")
            startLocationTextView.text = it.getString("start_location", "서울특별시 영등포구 여의동로 330")
            periodTextView.text = it.getString("period", "2025. 01. 26 ~ 2025. 02. 01")
            countTextView.text = it.getString("count", "제한 없음")
            notice1TextView.text = it.getString("notice1", "이 마라톤은 누구나 참여할 수 있으며, 정해진 코스를 따라야 기록이 인정됩니다.")
            notice2TextView.text = it.getString("notice2", "정해진 출발 시간 없음, 기간 내 언제든지 원하는 시간에 완주하면 인정됩니다.")
        }

        // 도전하기 버튼 클릭 이벤트
        challengeButton.setOnClickListener {
            // TODO: 도전하기 버튼 다음 화면
        }

        return view
    }

    companion object {
        fun newInstance(
            title: String,
            location: String,
            distance: String,
            reward: String,
            startLocation: String,
            period: String,
            count: String,
            notice1: String,
            notice2: String
        ): MarathonInformationFragment {
            val fragment = MarathonInformationFragment()
            val args = Bundle().apply {
                putString("title", title)
                putString("location", location)
                putString("distance", distance)
                putString("reward", reward)
                putString("start_location", startLocation)
                putString("period", period)
                putString("count", count)
                putString("notice1", notice1)
                putString("notice2", notice2)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

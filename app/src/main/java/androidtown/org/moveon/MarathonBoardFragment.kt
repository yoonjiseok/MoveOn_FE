package androidtown.org.moveon.repository

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidtown.org.moveon.R
import androidtown.org.moveon.MarathonInformationFragment
import androidtown.org.moveon.MarathonInformationTapFragment

class MarathonBoardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_marathon_board, container, false)

        val cardSection = view.findViewById<LinearLayout>(R.id.cardSection)

        // 카드 클릭 시 MarathonInformationFragment로 이동
        cardSection.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, MarathonInformationTapFragment()) // fragment_container는 메인 액티비티의 Fragment 레이아웃 ID
                addToBackStack(null) // 뒤로 가기 가능하도록 백스택 추가
            }
        }

        return view
    }
}

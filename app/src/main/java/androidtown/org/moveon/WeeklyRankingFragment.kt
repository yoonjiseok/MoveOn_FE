package androidtown.org.moveon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class WeeklyRankingFragment : Fragment() {
    private lateinit var weekSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weekly_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Spinner 초기화
        weekSpinner = view.findViewById(R.id.weekSpinner)

        // Spinner 데이터 설정
        setupWeekSpinner()
    }

    private fun setupWeekSpinner() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

        // 월요일 기준으로 설정
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weeks = mutableListOf<String>()

        // 최대 12주 전부터 현재 주까지 추가
        for (i in 0..11) {
            val startOfWeek = dateFormat.format(calendar.time)
            calendar.add(Calendar.DATE, 6) // 일요일로 이동
            val endOfWeek = dateFormat.format(calendar.time)
            weeks.add("$startOfWeek ~ $endOfWeek")

            calendar.add(Calendar.DATE, -6) // 다시 월요일로 돌아감
            calendar.add(Calendar.WEEK_OF_YEAR, -1) // 이전 주로 이동
        }

        // Spinner에 데이터 연결
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            weeks
        )
        weekSpinner.adapter = adapter

        // 기본 선택 (현재 주)
        weekSpinner.setSelection(0)

        // Spinner 항목 선택 이벤트
        weekSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedRange = weeks[position]
                fetchRankingDataForWeek(selectedRange)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 것도 선택되지 않았을 때 처리 (필요 시 구현)
            }
        }
    }

    private fun fetchRankingDataForWeek(selectedRange: String) {
        // 선택된 주에 대한 데이터를 서버로부터 가져오는 로직 구현
        // 예: 서버 API 호출 또는 로컬 데이터 처리
    }
}

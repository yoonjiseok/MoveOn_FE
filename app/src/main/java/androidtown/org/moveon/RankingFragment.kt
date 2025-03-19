package androidtown.org.moveon

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidtown.org.moveon.api.RankingService
import androidtown.org.moveon.api.RetrofitClient
import androidtown.org.moveon.api.WeeklyRankingResponse
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RankingFragment : Fragment() {
    private lateinit var weekSpinner: Spinner
    private lateinit var myScoreTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingListAdapter
    private val rankingService = RetrofitClient.instance.create(RankingService::class.java)
    private val currentUserId = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 요소 초기화
        recyclerView = view.findViewById(R.id.rankingRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RankingListAdapter(emptyList(), currentUserId)
        recyclerView.adapter = adapter

        weekSpinner = view.findViewById(R.id.weekSpinner)
        myScoreTextView = view.findViewById(R.id.myScoreTextView)

        // 주간 랭킹 제목 설정
        val rankingTitle = view.findViewById<TextView>(R.id.pageTitle)
        rankingTitle.text = "주간 랭킹"

        // 현재 날짜 기준 랭킹 데이터 가져오기
        val currentDate = getCurrentDate()
        fetchRankingData(currentDate)

        // Spinner 설정
        setupWeekSpinner()
    }

    private fun setupWeekSpinner() {
        val weeks = getWeeksList()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            weeks
        )
        weekSpinner.adapter = adapter

        // 기본 선택 (현재 주)
        weekSpinner.setSelection(0)

        weekSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedWeek = weeks[position]
                val lookupDate = selectedWeek.split("~")[0].trim()
                fetchRankingData(lookupDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 것도 선택되지 않았을 때 처리
            }
        }
    }

    private fun getWeeksList(): List<String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val weeks = mutableListOf<String>()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        for (i in 0 until 12) {
            val startOfWeek = dateFormat.format(calendar.time)
            calendar.add(Calendar.DATE, 6)
            val endOfWeek = dateFormat.format(calendar.time)
            weeks.add("$startOfWeek ~ $endOfWeek")

            calendar.add(Calendar.DATE, -6)
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
        }
        return weeks
    }

    private fun fetchRankingData(lookupDate: String) {
        rankingService.getWeeklyRanking(lookupDate).enqueue(object : Callback<WeeklyRankingResponse> {
            override fun onResponse(call: Call<WeeklyRankingResponse>, response: Response<WeeklyRankingResponse>) {
                if (response.isSuccessful) {
                    val rankingList = response.body()?.data ?: emptyList()
                    adapter.updateList(rankingList)
                } else {
                    Log.e("RankingFragment", "Error Code: ${response.code()}, Message: ${response.message()}")
                    Toast.makeText(requireContext(), "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeeklyRankingResponse>, t: Throwable) {
                Log.e("RankingFragment", "Network Failure: ${t.message}")
                Toast.makeText(requireContext(), "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

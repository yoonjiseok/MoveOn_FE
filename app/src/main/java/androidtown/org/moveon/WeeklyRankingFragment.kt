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
import androidtown.org.moveon.api.RankItem
import androidtown.org.moveon.api.RankingService
import androidtown.org.moveon.api.RetrofitClient
import androidtown.org.moveon.api.WeeklyRankingResponse
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WeeklyRankingFragment : Fragment() {
    private lateinit var weekSpinner: Spinner
    private lateinit var myScoreTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingListAdapter
    private val rankingService = RetrofitClient.instance.create(RankingService::class.java)
    val currentUserId = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weekly_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.rankingRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RankingListAdapter(emptyList(), currentUserId)
        recyclerView.adapter = adapter

        val currentDate = getCurrentDate()
        fetchRankingData(currentDate)

        // Spinner 초기화
        weekSpinner = view.findViewById(R.id.weekSpinner)
        myScoreTextView = view.findViewById(R.id.myScoreTextView)

        // Spinner 데이터 설정
        setupWeekSpinner()
    }

    private fun setupWeekSpinner() {
        val weeks = getWeeksList()

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
                val selectedWeek = weeks[position]
                val lookupDate = selectedWeek.split("~")[0].trim()
                fetchRankingData((lookupDate))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 것도 선택되지 않았을 때 처리 (필요 시 구현)
            }
        }
    }
    private fun getWeeksList(): List<String>{
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        val weeks = mutableListOf<String>()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)

        for (i in 0 until 12) {
            val startOfWeek = dateFormat.format(calendar.time)
            calendar.add(java.util.Calendar.DATE, 6)
            val endOfWeek = dateFormat.format(calendar.time)
            weeks.add("$startOfWeek ~ $endOfWeek")

            calendar.add(java.util.Calendar.DATE, -6)
            calendar.add(java.util.Calendar.WEEK_OF_YEAR, -1)
        }
        return weeks
    }
    private fun fetchRankingData(lookupDate: String) {
        rankingService.getWeeklyRanking(lookupDate).enqueue(object : Callback<WeeklyRankingResponse>{
            override fun onResponse(call: Call<WeeklyRankingResponse>, response: Response<WeeklyRankingResponse>){
                if (response.isSuccessful){
                    val rankingList = response.body()?.data ?: emptyList()
                    adapter.updateList(rankingList)
                } else{
                    Log.e("WeeklyRanking", "Error Code: ${response.code()}, Message: ${response.message()}")
                    Toast.makeText(requireContext(), "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeeklyRankingResponse>, t: Throwable) {
                Log.e("WeeklyRanking", "Network Failure: ${t.message}")
                Toast.makeText(requireContext(), "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getCurrentDate():String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

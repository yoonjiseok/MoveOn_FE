package androidtown.org.moveon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class WeeklyStatsFragment : Fragment() {

    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weekly_states, container, false)
        barChart = view.findViewById(R.id.barChart)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBarChart()
    }

    private fun setupBarChart() {
        val entries = listOf(
            BarEntry(0f, 30f),
            BarEntry(1f, 45f),
            BarEntry(2f, 50f),
            BarEntry(3f, 40f),
            BarEntry(4f, 20f),
            BarEntry(5f, 15f),
            BarEntry(6f, 10f)
        )
        val dataSet = BarDataSet(entries, "주간 칸 수")
        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate()
    }
}

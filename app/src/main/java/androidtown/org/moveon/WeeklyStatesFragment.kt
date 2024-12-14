package androidtown.org.moveon;

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class WeeklyStatesFragment : Fragment() {

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
        // BarChart 스타일 설정
        barChart.description.isEnabled = false // Description 제거
        barChart.setDrawGridBackground(false) // 그리드 배경 제거
        barChart.setDrawBorders(false) // 테두리 제거
        barChart.axisRight.isEnabled = false // 오른쪽 Y축 제거
        barChart.axisLeft.isEnabled = false // 왼쪽 Y축 활성화
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM // X축 위치
        barChart.xAxis.setDrawGridLines(false) // X축 그리드 제거
        barChart.xAxis.setDrawAxisLine(false) // X축 선 제거
        barChart.xAxis.granularity = 1f
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("월", "화", "수", "목", "금", "토", "일"))
        barChart.xAxis.textSize=14f
        barChart.xAxis.textColor = Color.parseColor("#4A5660")
        barChart.legend.isEnabled=false
        barChart.setTouchEnabled(false)
        // Y축 스타일 설정
        val yAxis = barChart.axisLeft
        yAxis.axisMinimum = 0f // 최소값 설정
        yAxis.axisMaximum = 60f
        yAxis.setDrawGridLines(false) // 그리드 제거
        yAxis.setDrawAxisLine(false) // 선 제거

        // 여백 설정
        barChart.setExtraOffsets(10f, 10f, 10f, 10f) // (left, top, right, bottom)


        val entries = listOf(
            BarEntry(0f, 30f),
            BarEntry(1f, 45f),
            BarEntry(2f, 50f),
            BarEntry(3f, 40f),
            BarEntry(4f, 20f),
            BarEntry(5f, 15f),
            BarEntry(6f, 10f)
        )

        // 가장 큰 값 찾기
        val maxValue = entries.maxOf { it.y }
        val maxIndex = entries.indexOfFirst { it.y == maxValue }

        val dataSet = BarDataSet(entries, "")
        dataSet.color = Color.parseColor("#D9EED9") // 바 색상 설정
        dataSet.valueTextColor = Color.parseColor("#4A5660") // 값 텍스트 색상 설정
        dataSet.valueTextSize = 14f // 텍스트 크기
        dataSet.setDrawValues(true) // 값 표시 활성화

        // 값 텍스트 색상을 설정
        val valueColors = entries.mapIndexed { index, _ ->
            if (index == maxIndex) Color.parseColor("#398342") // 최대값 텍스트 색상
            else Color.parseColor("#4A5660") // 기본 텍스트 색상
        }
        dataSet.setValueTextColors(valueColors) // 수정된 색상 리스트를 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.7f
        barChart.data = barData

        // 둥근 모서리 렌더러 적용
        barChart.renderer = RoundedBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler)
        barChart.invalidate()
    }
    // 둥근 모서리를 적용하는 BarChartRenderer 클래스
    inner class RoundedBarChartRenderer(
        chart: BarChart,
        animator: com.github.mikephil.charting.animation.ChartAnimator,
        viewPortHandler: ViewPortHandler
    ) : BarChartRenderer(chart, animator, viewPortHandler) {

        private val roundedRect = RectF()
        private val path = Path()

        override fun drawDataSet(
            c: Canvas,
            dataSet: com.github.mikephil.charting.interfaces.datasets.IBarDataSet,
            index: Int
        ) {
            val barData = mChart.barData
            val barWidthHalf = barData.barWidth / 2.0f

            val trans = mChart.getTransformer(dataSet.axisDependency)

            mBarBorderPaint.style = Paint.Style.FILL

            for (j in 0 until dataSet.entryCount) {
                val e = dataSet.getEntryForIndex(j) as BarEntry

                // mBarRect 값을 설정
                mBarRect.set(
                    e.x - barWidthHalf,
                    0f.coerceAtLeast(e.y), // top
                    e.x + barWidthHalf,
                    0f.coerceAtMost(e.y)  // bottom
                )

                trans.rectToPixelPhase(mBarRect, mAnimator.phaseY)

                roundedRect.set(
                    mBarRect.left,
                    mBarRect.top,
                    mBarRect.right,
                    mBarRect.bottom
                )

                // 둥근 모서리 반지름 설정
                val radius = 70f
                path.reset()
                path.addRoundRect(roundedRect, radius, radius, Path.Direction.CW)
                mRenderPaint.color = dataSet.getColor(j)
                c.drawPath(path, mRenderPaint)

                // 값 텍스트 표시
                if (dataSet.isDrawValuesEnabled) {
                    val valueText = dataSet.valueFormatter.getBarLabel(e)
                    val valueX = (roundedRect.left + roundedRect.right) / 2
                    val valueY = roundedRect.top - 10f // 텍스트를 막대 위에 표시
                    drawValue(c, valueText, valueX, valueY, dataSet.getValueTextColor(j))
                }
            }
        }
        override fun drawValue(
            c: Canvas,
            valueText: String,
            x: Float,
            y: Float,
            color: Int
        ) {
            mValuePaint.color = color
            mValuePaint.textSize = 40f // 텍스트 크기 설정 (예: 40f)
            mValuePaint.textAlign = Paint.Align.CENTER // 텍스트 가운데 정렬
            c.drawText(valueText, x, y, mValuePaint)
        }
    }
}

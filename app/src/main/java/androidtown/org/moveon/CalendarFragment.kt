package androidtown.org.moveon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var currentMonthText: TextView
    private val calendar: Calendar = Calendar.getInstance()

    // 오늘 날짜와 선택된 날짜를 저장하는 변수
    private val todayDate: Date = calendar.time
    private var selectedDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        currentMonthText = view.findViewById(R.id.currentMonth)

        val prevMonth = view.findViewById<View>(R.id.prevMonth)
        val nextMonth = view.findViewById<View>(R.id.nextMonth)

        // 이전/다음 달 이동 버튼
        prevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        nextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        updateCalendar()
        return view
    }

    private fun updateCalendar() {
        val dateFormat = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())
        currentMonthText.text = dateFormat.format(calendar.time)

        val dates = generateDatesForMonth()

        // 선택된 날짜가 없으면 오늘 날짜를 기본값으로 설정
        if (selectedDate == null) {
            selectedDate = todayDate
        }

        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
        calendarRecyclerView.adapter = CalendarAdapter(dates) { selectedDate ->
            onDateSelected(selectedDate)
        }
    }

    private fun generateDatesForMonth(): List<Date> {
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDay = tempCalendar.time

        tempCalendar.set(Calendar.DAY_OF_MONTH, tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDay = tempCalendar.time
        val dates = mutableListOf<Date>()
        tempCalendar.time = startDay
        while(!tempCalendar.time.after(endDay)){
            dates.add(tempCalendar.time)
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dates
    }

    private fun onDateSelected(date: Date) {
        // 날짜 선택 시 이벤트 처리
        selectedDate = date
        updateCalendar() // RecyclerView를 갱신하여 선택 상태를 반영
    }

    // Adapter for Calendar
    inner class CalendarAdapter(
        private val dates: List<Date>,
        private val onClick: (Date) -> Unit
    ) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

        inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar, parent, false)
            return CalendarViewHolder(view)
        }

        override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
            val date = dates[position]
            val dateFormat = SimpleDateFormat("d", Locale.getDefault())
            holder.tvDate.text = dateFormat.format(date)
            val today = Calendar.getInstance()
            val calendar = Calendar.getInstance().apply { time = date }

            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
            {
                holder.tvDate.setBackgroundResource(R.drawable.circle_highlight)
                holder.tvDate.setTextColor(resources.getColor(R.color.white, null))
            } else{
                holder.tvDate.background = null
                holder.tvDate.setTextColor(resources.getColor(R.color.custom_gray, null))
            }
            // 오늘 날짜 하이라이트 처리
            if (selectedDate == null && date == todayDate) {
                holder.tvDate.setBackgroundResource(R.drawable.circle_highlight)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(android.R.color.white)) // 흰색 텍스트
            }
            // 선택된 날짜 하이라이트 처리
            else if (selectedDate != null && date == selectedDate) {
                holder.tvDate.setBackgroundResource(R.drawable.circle_highlight)
                holder.tvDate.setTextColor(holder.itemView.context.getColor(android.R.color.white)) // 흰색 텍스트
            }
            // 기본 상태 처리
            else {
                holder.tvDate.background = null
                holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.custom_gray)) // 기본 텍스트 색상
            }

            holder.itemView.setOnClickListener {
                selectedDate = date
                notifyDataSetChanged() // RecyclerView를 갱신하여 선택 상태 반영
                onClick(date)
            }
        }

        override fun getItemCount(): Int = dates.size
    }
}

package androidtown.org.moveon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.tabs.TabLayout

class RecommendedCourseDialog : DialogFragment() {
    private var currentIndex = 0
    private var isCustomTab = true
    
    private val customCourses = listOf(
        CourseInfo("여의도 한강공원", "서울", "10Km"),
        CourseInfo("북한산 둘레길", "서울", "8Km"),
        CourseInfo("부산 해운대", "부산", "5Km")
    )
    
    private val weeklyCourses = listOf(
        CourseInfo("여의도 한강공원", "서울", "12Km"),
        CourseInfo("북한산 둘레길", "서울", "12Km"),
        CourseInfo("부산 해운대", "부산", "12Km")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomAlertDialog)
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val displayMetrics = requireContext().resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.9).toInt() // 화면 너비의 90%
            window.setLayout(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_recommended_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val btnPrevious = view.findViewById<ImageButton>(R.id.btnPrevious)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNext)
        val courseTitle = view.findViewById<TextView>(R.id.courseTitle)
        val locationInfo = view.findViewById<TextView>(R.id.locationInfo)
        val distanceInfo = view.findViewById<TextView>(R.id.distanceInfo)
        val recommendationType = view.findViewById<TextView>(R.id.recommendationType)

        // 탭 선택 리스너
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        isCustomTab = true
                        recommendationType.text = "'Username'님을 위한 추천코스"
                        updateCourseInfo(currentIndex)
                    }
                    1 -> {
                        isCustomTab = false
                        recommendationType.text = "Move On이 추천하는 코스"
                        updateCourseInfo(currentIndex)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 이전/다음 버튼 리스너
        btnPrevious.setOnClickListener {
            val courses = if (isCustomTab) customCourses else weeklyCourses
            if (currentIndex > 0) {
                currentIndex--
                updateCourseInfo(currentIndex)
            }
        }

        btnNext.setOnClickListener {
            val courses = if (isCustomTab) customCourses else weeklyCourses
            if (currentIndex < courses.size - 1) {
                currentIndex++
                updateCourseInfo(currentIndex)
            }
        }


        // 초기 코스 정보 표시
        updateCourseInfo(currentIndex)
    }

    private fun updateCourseInfo(index: Int) {
        val courses = if (isCustomTab) customCourses else weeklyCourses
        val course = courses[index]
        view?.apply {
            findViewById<TextView>(R.id.courseTitle).text = course.title
            findViewById<TextView>(R.id.locationInfo).text = course.location
            findViewById<TextView>(R.id.distanceInfo).text = course.distance
            
            // 이전/다음 버튼 활성화 상태 업데이트
            findViewById<ImageButton>(R.id.btnPrevious).isEnabled = index > 0
            findViewById<ImageButton>(R.id.btnNext).isEnabled = index < courses.size - 1
        }
    }

    private data class CourseInfo(
        val title: String,
        val location: String,
        val distance: String
    )
}
package androidtown.org.moveon

import android.os.Bundle
import android.text.TextUtils.replace
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class ActivityRecordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_activity_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Calendar Fragment 추가
        childFragmentManager.commit {
            replace(R.id.calendar_fragment_container, CalendarFragment())
        }

        // Weekly Stats Fragment 추가
        childFragmentManager.commit {
            replace(R.id.weekly_states_fragment_container, WeeklyStatesFragment())
        }
    }
}

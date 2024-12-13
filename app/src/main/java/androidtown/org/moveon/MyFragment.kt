package androidtown.org.moveon;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidtown.org.moveon.R
import androidx.fragment.app.Fragment

class MyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my, container, false)

        val activityRecordSection : View = view.findViewById(R.id.activityRecordSectoin)
        activityRecordSection.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ActivityRecordFragment())
                .addToBackStack(null)
                .commit()
        }
        return view
    }
}

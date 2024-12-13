package androidtown.org.moveon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class RunningFragment : Fragment(R.layout.fragment_running) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startButton = view.findViewById<Button>(R.id.startButton) // fragment_running.xml의 버튼
        startButton?.setOnClickListener {
            // MainRecordFragment로 이동
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainRecordFragment())
                .addToBackStack(null) // 뒤로 가기 버튼 동작 설정
                .commit()
        }
    }
}

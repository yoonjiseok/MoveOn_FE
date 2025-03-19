package androidtown.org.moveon

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MarathonAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
        0 -> MarathonInformationFragment()
        1 -> MarathonRankingFragment()
    else -> throw IllegalStateException("Invalid position")
        }
    }
}
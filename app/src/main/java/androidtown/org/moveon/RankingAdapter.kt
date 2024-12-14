package androidtown.org.moveon

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class RankingAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MarathonRankingFragment()
            1 -> WeeklyRankingFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}

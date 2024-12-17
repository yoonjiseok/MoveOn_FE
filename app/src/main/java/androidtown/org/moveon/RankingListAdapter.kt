package androidtown.org.moveon

import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidtown.org.moveon.api.RankItem
import androidx.recyclerview.widget.RecyclerView


class RankingListAdapter(
    private var rankingList: List<RankItem>,
    private val currentUserId: Int
) : RecyclerView.Adapter<RankingListAdapter.RankingViewHolder>() {
    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankTextView: TextView = itemView.findViewById(R.id.rankTextView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
        val crownImageView: ImageView = itemView.findViewById(R.id.crownImageView)
        val container: View = itemView.findViewById(R.id.itemContainer)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val rankItem = rankingList[position]

        when(rankItem.rank){
            1 -> {
                holder.crownImageView.setImageResource(R.drawable.ic_gold_crown)
                holder.crownImageView.visibility = View.VISIBLE
            }
            2 -> {
                holder.crownImageView.setImageResource(R.drawable.ic_silver_crown)
                holder.crownImageView.visibility = View.VISIBLE
            }
            3 -> {
                holder.crownImageView.setImageResource(R.drawable.ic_bronze_crown)
                holder.crownImageView.visibility = View.VISIBLE
            }
            else -> {
                holder.crownImageView.visibility = View.GONE
            }
        }
        if (rankItem.userId == currentUserId){
            holder.container.setBackgroundResource(R.drawable.my_rank_background)//수정 필요
            holder.usernameTextView.text = "Me"
        } else {
            holder.container.setBackgroundResource(R.drawable.rank_item_background)
            holder.usernameTextView.text = "${rankItem.nickname}"
        }

        holder.rankTextView.text = if (rankItem.rank > 3) rankItem.rank.toString() else ""
        holder.scoreTextView.text = "${rankItem.currentPixelCount}"
    }

    override fun getItemCount(): Int = rankingList.size

    fun updateList(newList: List<RankItem>) {
        rankingList = newList
        notifyDataSetChanged()
    }
}

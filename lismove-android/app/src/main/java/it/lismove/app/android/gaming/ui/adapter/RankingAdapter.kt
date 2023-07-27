package it.lismove.app.android.gaming.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ItemRankingBinding
import it.lismove.app.android.gaming.ui.data.RankingItemUI

class RankingAdapter (
    var items: List<RankingItemUI>
    ): RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val binding = ItemRankingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    class RankingViewHolder(val binding: ItemRankingBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: RankingItemUI){
            with(binding){
                position.text = item.position
                positionLabel.text = item.positionLabel
                userName.text = item.username
                points.text = item.points
                pointsLabel.text = item.pointsLabel
                binding.root.setBackgroundResource(item.background)

                if(item.picProfile.isNullOrEmpty()){
                    val avatarPic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab)

                    picProfile.load(avatarPic)
                }else{
                    picProfile.load(item.picProfile){
                        transformations(CircleCropTransformation())
                    }
                }


            }
        }
    }
}
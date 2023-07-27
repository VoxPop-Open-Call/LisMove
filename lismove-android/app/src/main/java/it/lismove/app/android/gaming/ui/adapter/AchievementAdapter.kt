package it.lismove.app.android.gaming.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ItemAchievementBinding
import it.lismove.app.android.gaming.ui.data.AchievementItemUI


class AchievementAdapter (
    var items: List<AchievementItemUI>,
    var onClick: (AchievementItemUI)->Unit
): RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.count()

    class AchievementViewHolder(val binding: ItemAchievementBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: AchievementItemUI, onClick: (AchievementItemUI) -> Unit){
            with(binding){
                achievementTitle.text = item.title
                achevementSubtitle.text = item.projectName
                root.setOnClickListener { onClick(item) }

                achievementProgressLabel.isVisible = item.fulfilled.not()
                achievementProgressLabel.text = item.percentageString

                achievementProgressBar.isVisible = item.fulfilled.not()
                achievementProgressBar.progress = item.percentageValue

                achievementFulfilledTargetLabel.isVisible = item.fulfilled
                achievementFulfilledTargetLabel.text = item.percentageString

                achievementFulfilledImage.isVisible = item.fulfilled
                achevementDaysCounter.isVisible = item.daysCounter != null
                achevementDaysCounter.text = item.daysCounter
                if(item.imageUrl.isNullOrEmpty()){
                    val avatarPic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab)

                    achievementImage.load(avatarPic)
                }else{
                    achievementImage.load(item.imageUrl){
                        transformations(CircleCropTransformation())
                    }
                }


            }
        }
    }
}
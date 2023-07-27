package it.lismove.app.android.dashboard.itemViews.items

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.faltenreich.skeletonlayout.createSkeleton
import com.xwray.groupie.viewbinding.BindableItem
import it.lismove.app.android.R
import it.lismove.app.android.dashboard.itemViews.data.ImageListDashboardItemData
import it.lismove.app.android.dashboard.itemViews.data.RankingDashboardItemData
import it.lismove.app.android.databinding.ItemDashboardImageListBinding
import it.lismove.app.android.databinding.ItemDashboardRankingBinding
import it.lismove.app.android.databinding.ItemDashboardSingleRankingBinding


class RankingDashboardItem(val data: RankingDashboardItemData,
                           val viewBinding: ItemDashboardRankingBinding) {

    fun bind() {
        with(viewBinding) {
            root.isVisible = true
            pointsListView.removeAllViews()
            val layoutInflater = LayoutInflater.from(root.context)

            data.rankings.forEach {
                val binding = ItemDashboardSingleRankingBinding.inflate(layoutInflater, pointsListView, true)

                binding.rankingInitiative.text = it.activeInitiative
                binding.rankingPoints.text = it.points.toString()

                with(binding){
                   if(it.iconRes.isNullOrEmpty()){
                       val pic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab_2)
                       rankingLogo.load(pic)
                   }else{
                       rankingLogo.load(it.iconRes){
                           transformations(CircleCropTransformation())
                       }
                   }
               }
            }
        }

    }

}
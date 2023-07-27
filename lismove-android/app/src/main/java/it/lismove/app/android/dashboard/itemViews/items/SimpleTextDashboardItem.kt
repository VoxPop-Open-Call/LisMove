package it.lismove.app.android.dashboard.itemViews.items

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.faltenreich.skeletonlayout.createSkeleton
import it.lismove.app.android.dashboard.itemViews.data.SimpleTextDashboardItemData
import it.lismove.app.android.databinding.ItemDashboardSimpleTextBinding

class SimpleTextDashboardItem(val data: SimpleTextDashboardItemData,
                              val binding: ItemDashboardSimpleTextBinding) {
    fun bind(){
        with(binding){
            root.isVisible = true
            with(dashboardItemImage){
                isVisible = data.leftIconRes != null
                data.leftIconRes?.let {
                    val imageDrawable = AppCompatResources.getDrawable(binding.root.context,it)
                    setImageDrawable(imageDrawable)
                }
            }

            dashboardItemText.text = data.mainText
            dashboardItemImage.isVisible = data.leftIconRes != null
            dashboardItemTextLabel.text = data.leftSmallText
            dashboardItemTextLabel.isVisible = data.leftSmallText.isNullOrEmpty().not()


        }
    }
}
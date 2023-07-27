package it.lismove.app.android.dashboard.itemViews.items

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import coil.load
import coil.transform.CircleCropTransformation
import com.faltenreich.skeletonlayout.createSkeleton
import it.lismove.app.android.R
import it.lismove.app.android.dashboard.itemViews.data.ProfileDashboardItemData
import it.lismove.app.android.databinding.ItemDashboardProfileBinding
import timber.log.Timber

class ProfileDashboardItem(val data: ProfileDashboardItemData,
                           val binding: ItemDashboardProfileBinding){

    fun bind() {
        with(binding){
            root.isVisible = true
            dashboardItemText.text = data.text
            dashboardItemAvgKmRight.text = data.avgRightText
            dashboardItemAvgKmLeft.text = data.avgLeftText
            dashboardItemAvgKmGroup.isVisible = true

            Timber.d("Redrawing profile with ${data.text}")
            if(data.image.isNullOrEmpty()){
                val pic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab)
                dashboardProfileImage.load(pic)
            }else{
                dashboardProfileImage.load(data.image){
                    transformations(CircleCropTransformation())
                }
            }
        }

    }


}
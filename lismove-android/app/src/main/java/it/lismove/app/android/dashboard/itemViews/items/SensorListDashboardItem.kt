package it.lismove.app.android.dashboard.itemViews.items

import android.content.res.ColorStateList
import android.view.animation.AnimationUtils
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.faltenreich.skeletonlayout.createSkeleton
import it.lismove.app.android.R
import it.lismove.app.android.dashboard.itemViews.data.SensorListDashboardItemData
import it.lismove.app.android.databinding.ItemDashboardSensorListBinding

class SensorListDashboardItem(val data: SensorListDashboardItemData,
                              val viewBinding: ItemDashboardSensorListBinding) {
    fun bind() {
        with(viewBinding) {
            root.isVisible = true
            val sensor = data.sensorListData.sensorList.firstOrNull()
            sensorItemChip.isVisible = sensor != null
            sensor?.let {
                sensorItemChip.text = it.sensorName
                val chipBackground = if(sensor.sensorConnected) R.color.GreenColor2 else R.color.chipBackgroundLight
                val chipText= if(sensor.sensorConnected) R.color.white else R.color.gray_image_tint
                val chipIcon= if(sensor.sensorConnected) R.drawable.ic_baseline_bluetooth_connected_24 else R.drawable.ic_baseline_bluetooth_disabled_24
                val chipDrawable = AppCompatResources.getDrawable(root.context, chipIcon)

                sensorItemChip.chipIcon = chipDrawable
                sensorItemChip.chipBackgroundColor = ColorStateList.valueOf( root.resources.getColor(chipBackground, null))
                sensorItemChip.chipIconTint = ColorStateList.valueOf(root.resources.getColor(chipText, null))
                sensorItemChip.setTextColor(ColorStateList.valueOf(root.resources.getColor(chipText, null)))

                refreshButton.setOnClickListener { data.sensorListData.onRefreshRequested() }

            }
            if(data.sensorListData.isLoading){
                refreshButton.startAnimation(AnimationUtils.loadAnimation(root.context, R.anim.clockwise))
            }else{
                refreshButton.clearAnimation()
            }


        }

    }
}


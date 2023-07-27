package it.lismove.app.android.dashboard.itemViews.items

import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import coil.load
import com.faltenreich.skeletonlayout.createSkeleton
import it.lismove.app.android.R
import it.lismove.app.android.dashboard.itemViews.data.ImageListDashboardItemData
import it.lismove.app.android.databinding.ItemDashboardImageListBinding
import it.lismove.app.android.initiative.ui.view.InitiativeRegulationAlertUtils


class ImageListDashboardItem(val data: ImageListDashboardItemData,
                             val viewBinding: ItemDashboardImageListBinding) {

    fun bind() {
        with(viewBinding){
            root.isVisible = true
            projectsList.removeAllViews()
            projectsList.isVisible = data.images.isNotEmpty()
            emptyString.isVisible = data.images.isEmpty()
            emptyString.text = data.emptyImagesText

            data.images.forEach { initiative ->
                val image = ImageView(viewBinding.root.context)
                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    150
                )
                params.setMargins(16, 16, 16, 16)

                image.setOnClickListener {
                    InitiativeRegulationAlertUtils.getAlertDialog(
                        viewBinding.root.context,
                        initiative.initiativeRule,
                        initiative.title ?: ""
                    ).show()
                }
                image.setPadding(2)
                image.setBackgroundResource(R.drawable.shape_border)
                image.scaleType = ImageView.ScaleType.CENTER_INSIDE
                image.layoutParams = params
                image.load(initiative.imageRes)
                projectsList.addView(image)

            }
        }

    }

}
package it.lismove.app.android.dashboard.itemViews

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faltenreich.skeletonlayout.createSkeleton
import com.xwray.groupie.viewbinding.BindableItem
import it.lismove.app.android.R
import it.lismove.app.android.dashboard.itemViews.data.*
import it.lismove.app.android.dashboard.itemViews.items.*
import it.lismove.app.android.databinding.ItemDashboardAllBinding

class DashBoardItem(var data: DashboardItemData): BindableItem<ItemDashboardAllBinding>()  {

    override fun bind(viewBinding: ItemDashboardAllBinding, position: Int) {
        with(viewBinding){
            bindDashboardCommonUI(data, viewBinding)
            profileDashboardItem.root.isVisible = false
            simpleTextDashboardItem.root.isVisible = false
            imageListDashboardItem.root.isVisible = false
            rankingsDashboardItem.root.isVisible = false
            chartDashboardItem.root.isVisible = false
            sensorListDashboardItem.root.isVisible = false

            data.let { data ->
                when(data){
                    is SimpleTextDashboardItemData -> SimpleTextDashboardItem(data, simpleTextDashboardItem).bind()
                    is ImageListDashboardItemData -> ImageListDashboardItem(data, imageListDashboardItem).bind()
                    is ProfileDashboardItemData -> ProfileDashboardItem(data, profileDashboardItem).bind()
                    is RankingDashboardItemData -> RankingDashboardItem(data, rankingsDashboardItem).bind()
                    is ChartDashboardItemData -> ChartDashboardItem(data, chartDashboardItem).bind()
                    is SensorListDashboardItemData -> SensorListDashboardItem(data, sensorListDashboardItem).bind()
                }
            }

        }
    }

    private fun bindDashboardCommonUI(item: DashboardItemData, viewBinding: ItemDashboardAllBinding){
        with(viewBinding){
            dashboardItemTitle.text = item.title
            loadingView.isIndeterminate = true
            loadingView.isVisible = item.isLoading
            body.isVisible = item.isLoading.not() && item.errorString.isNullOrEmpty()
            errorLabel.isVisible = item.errorString.isNullOrEmpty().not()
            val layoutParams = root.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.isFullSpan = item.stretched
            root.setOnClickListener(null)
            root.setOnClickListener {
                data.onCardClicked()
               showInfoAlertIfPresent(data, root.context)
            }
        }
    }

    fun showInfoAlertIfPresent(item: DashboardItemData, context: Context){
        item.alertDescription?.let {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(item.alertDescription)
            builder.setPositiveButton("chiudi") { dialog, which -> }
            builder.show()
        }
    }

    override fun initializeViewBinding(view: View): ItemDashboardAllBinding {
        return ItemDashboardAllBinding.bind(view)
    }

    override fun getLayout(): Int {
        return R.layout.item_dashboard_all
    }

    override fun getDragDirs(): Int {
        return ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.END or ItemTouchHelper.START
    }


}
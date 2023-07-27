package it.lismove.app.android.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import it.lismove.app.android.R
import it.lismove.app.android.car.ui.CarConfigurationActivity
import it.lismove.app.android.dashboard.itemViews.DashBoardItem
import it.lismove.app.android.dashboard.itemViews.data.*
import it.lismove.app.android.databinding.FragmentDashboardBinding
import it.lismove.app.android.deviceConfiguration.DeviceConfigActivity
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.general.lce.*
import it.lismove.app.android.notification.ui.NotificationListActivity
import net.nextome.lismove_sdk.utils.BugsnagUtils
import it.lismove.app.android.profile.ProfileActivity
import it.lismove.app.android.session.ui.SessionsHistoryActivity
import it.lismove.app.android.settings.SensorDetailActivity
import it.lismove.app.room.entity.DashoardPositionEntity

import org.koin.android.ext.android.inject
import timber.log.Timber

class DashboardFragment : LisMoveFragment(R.layout.fragment_dashboard), LceView<List<DashboardItemData>>, DashboardCallback {
    lateinit var binding: FragmentDashboardBinding
    var dashboardAdapter = GroupAdapter<GroupieViewHolder>()
    val viewModel: DashboardViewModel by inject()
    var items = arrayListOf<DashBoardItem>()
    var section = Section()
    private val touchCallback: SwipeTouchCallback by lazy {
        object : SwipeTouchCallback() {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                val item = dashboardAdapter.getItem(viewHolder.bindingAdapterPosition)
                val targetItem = dashboardAdapter.getItem(target.bindingAdapterPosition)

                val dragItems = section.groups
                var targetIndex = dragItems.indexOf(targetItem)
                dragItems.remove(item)

                // if item gets moved out of the boundary
                if (targetIndex == -1) {
                    targetIndex = if (target.bindingAdapterPosition < viewHolder.bindingAdapterPosition) {
                        0
                    } else {
                        dragItems.size - 1
                    }
                }
                dragItems.add(targetIndex, item)
                section.update(dragItems)
                updatePosition()
                return true
            }

            fun updatePosition() {
                Timber.d("Update position")
                section.groups.forEachIndexed { index, group ->
                    val item = group as DashBoardItem
                    item.data.pos = index

                }
                viewModel.updatePosition(items)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

        }
    }

    private val itemTouchHelper = ItemTouchHelper(touchCallback)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Home"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)


        with(binding){
            dashBoardRecyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            itemTouchHelper.attachToRecyclerView(dashBoardRecyclerView)
            dashBoardRecyclerView.adapter = dashboardAdapter
        }

        Timber.d("onCreated")

        viewModel.errorObserver.observe(this) {onError(it)}
        viewModel.dashboardState.asLiveData().observe(requireActivity(), LceDispatcher(this))
        viewModel.dashboardUpdateObservable.observe(this){ updateItemAtPosition(it) }
        viewModel.deviceNotFoundObserver.observe(viewLifecycleOwner) { showSensorSnackBar() }

    }

    private fun updateItemAtPosition(pos: Int){
        try {
            val profileCardIndex = items.indexOfFirst { it.data.pos == pos}
            if (profileCardIndex == -1) return

            items[profileCardIndex].data = viewModel.items.first { it.pos == pos }
            dashboardAdapter.notifyItemChanged(profileCardIndex)
        }catch (e:Exception){
            var errorString = "updateItemAtPosition: index of $pos not found\n"
            items.forEach {
                errorString += "title:${it.data.title}, pos: ${it.data.pos},  "
            }

            // BugsnagUtils.logEvent(errorString)
            Timber.d(errorString)
        }

    }

    override fun onLoading() {
        Timber.d("onLoading")
        with(binding){
            loadingBar.isIndeterminate = true
            loadingBar.isVisible = true
            dashBoardRecyclerView.isVisible = false
        }
    }

    override fun onSuccess(data: List<DashboardItemData>) {
        Timber.d("onSuccess")
        populateItemList()
        if(dashboardAdapter.groupCount > 0){
            dashboardAdapter.remove(section)
        }
        section = Section()
        with(section){
            items.sortBy { it.data.pos }
            items.forEach {
                    add(it)
            }
        }
        dashboardAdapter.add(section)
        Timber.d("${section.groups.size}")
        Timber.d("${items.size}")

        with(binding){
            loadingBar.isVisible = false
            dashBoardRecyclerView.isVisible = true
        }
    }

    private fun populateItemList(){
       items = ArrayList(viewModel.items.map {DashBoardItem(it) })
    }


    override fun onError(throwable: Throwable) {
        Timber.d("onError ${throwable.localizedMessage}")
    }


    private fun showSensorSnackBar(){
        val deviceNotFoundSnackbar = Snackbar.make(binding.root, requireContext().getString(R.string.sensor_not_detected),
            Snackbar.LENGTH_LONG)

        if (viewModel.deviceNotFoundCounter >= 1) {
            deviceNotFoundSnackbar.setAction(R.string.pair_again) {
                startActivity(Intent(requireContext(), DeviceConfigActivity::class.java))
            }
        }

        deviceNotFoundSnackbar.show()
    }

    override fun onRefreshDeviceRequested() {
        context?.let {
            viewModel.requestDeviceRefresh(it)
        }
    }

    override fun onOpenNotification() {
        activity?.let {
            startActivity(Intent(it, NotificationListActivity::class.java))
        }
    }

    override fun onCardClicked(type: Int) {
        Timber.d("onCardClicked of type $type")
        when(type){
            DashoardPositionEntity.PROFILE -> goToProfile()
            DashoardPositionEntity.SENSOR -> goToSensorDetail()
            DashoardPositionEntity.KM_DONE -> goToSessionList()
            DashoardPositionEntity.CO2 -> startCo2()
        }
    }

    private fun startCo2() {/*
        context?.let {
            startActivity(Intent(it, CarConfigurationActivity::class.java))
        }*/
    }

    private fun goToSensorDetail() {
        context?.let {
            startActivity(Intent(it, SensorDetailActivity::class.java))
        }
    }

    private fun goToProfile(){
        context?.let {
            startActivity(Intent(it, ProfileActivity::class.java ))
        }
    }

    private fun goToSessionList(){
        context?.let {
            startActivity(SessionsHistoryActivity.getIntent(it, onlyWokPath = false))
        }
    }



    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        context?.let {
            viewModel.getDashboardData(it, this)
            viewModel.updateSensorData(it)
        }
    }
}

interface DashboardCallback{
    fun onRefreshDeviceRequested()
    fun onOpenNotification()
    fun onCardClicked(type: Int)
}
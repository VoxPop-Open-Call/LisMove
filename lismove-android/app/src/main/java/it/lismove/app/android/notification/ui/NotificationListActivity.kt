package it.lismove.app.android.notification.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import it.lismove.app.android.databinding.ActivityNotificationListBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.notification.ui.adapter.NotificationListAdapter
import it.lismove.app.android.notification.data.NotificationListItem
import org.koin.android.ext.android.inject

class NotificationListActivity : LisMoveBaseActivity(), LceView<List<NotificationListItem>> {
    lateinit var binding: ActivityNotificationListBinding
    val viewModel: NotificationListViewModel by inject()
    var notificationAdapter = NotificationListAdapter(
        listOf()
    ){ notificationListItem ->
        viewModel.setNotificationMessageSeen(notificationListItem)
    }

    companion object{
        val INTENT_NOTIFICATION_OPEN  = "INTENT_NOTIFICATION_OPEN"

        fun getIntent(ctx: Context, notificationId: Long?): Intent{
            return Intent(ctx, NotificationListActivity::class.java).apply {
                notificationId?.let {
                    putExtra(INTENT_NOTIFICATION_OPEN, it)

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setRecyclerView()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.swipeToRefreshLayout.setOnRefreshListener {
            refreshData()
        }
        viewModel.setDataFromIntent(intent)
        viewModel.stateObservable.observe(this, LceDispatcher(this))
        viewModel.reloadMessages(true)
    }

    private fun refreshData() {
        viewModel.reloadMessages(false)
    }

    private fun setRecyclerView(){
        with(binding.recyclerView){
            layoutManager = LinearLayoutManager(this@NotificationListActivity)
            adapter = notificationAdapter
            val itemDecoration =  DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
            addItemDecoration(itemDecoration)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onLoading() {
        binding.swipeToRefreshLayout.isRefreshing = true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSuccess(data: List<NotificationListItem>) {
        binding.swipeToRefreshLayout.isRefreshing = false
        notificationAdapter.items = data
        notificationAdapter.notifyDataSetChanged()


    }

    override fun onError(throwable: Throwable) {
        showError(throwable.localizedMessage ?: "Si è verificato un problema, riprova più tardi", binding.root)
        binding.swipeToRefreshLayout.isRefreshing = false

    }

}
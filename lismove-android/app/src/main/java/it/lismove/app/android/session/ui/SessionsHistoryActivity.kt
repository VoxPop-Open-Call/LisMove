package it.lismove.app.android.session.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivitySessionHistoryBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.session.adapter.SessionListAdapter
import it.lismove.app.android.session.ui.data.SessionListItemUI
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class SessionsHistoryActivity : LisMoveBaseActivity(), LceView<List<SessionListItemUI>> {
    lateinit var binding: ActivitySessionHistoryBinding
    val adapter = SessionListAdapter(listOf()){onItemClicked(it)}
    val viewModel: SessionsHistoryViewModel by inject()

    companion object{
        const val INTENT_ONLY_WORK_PATH = "INTENT_ONLY_WORK_PATH"
        const val SESSION_TITLE = "Le mie sessioni"
        const val WORK_PATH_SESSION_TITLE = "Le mie sessioni casa - scuola/lavoro"

        const val SESSION_TYPE_LABEL = "sessione"
        const val WORK_PATH_SESSION_TYPE_LABEL = "sessione casa - scuola/lavoro"
        fun getIntent(ctx: Context, onlyWokPath: Boolean = false): Intent{
            return Intent(ctx, SessionsHistoryActivity::class.java).apply {
                putExtra(INTENT_ONLY_WORK_PATH, onlyWokPath)
            }
        }
    }

    private var filterIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK){
         viewModel.getDateFromResultIntent(result.data)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
        setupRecyclerView()
        binding.loadingBar.isIndeterminate = true

        viewModel.state.observe(this, LceDispatcher(this))
        val onlyWokPath = intent.getBooleanExtra(INTENT_ONLY_WORK_PATH, false)
        title = if(onlyWokPath) WORK_PATH_SESSION_TITLE else SESSION_TITLE
        val sessionType = if(onlyWokPath) WORK_PATH_SESSION_TYPE_LABEL else SESSION_TYPE_LABEL
        binding.emptyListText.text = resources.getString(R.string.session_empty_label, sessionType)
        viewModel.getSessionData(onlyWokPath)
        viewModel.filterValueObservable.observe(this){
            binding.filterToolbarText.text = it
        }
        binding.filterToolbar.setOnClickListener {
            filterIntentLauncher.launch(FilterDatePickerActivity.getIntent(this, viewModel.startDate, viewModel.endDate))
        }
    }

    fun setupRecyclerView(){
        binding.sessionListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.sessionListRecyclerView.adapter = adapter
    }

    fun onItemClicked(item: SessionListItemUI){
        startActivity(SessionDetailActivity.getIntent(this@SessionsHistoryActivity, item.id, true))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onLoading() {
        Timber.d("Loading")
        binding.loadingBar.isVisible = true
        binding.successGroup.isVisible = false

    }

    override fun onSuccess(data: List<SessionListItemUI>) {
        binding.loadingBar.visibility = View.GONE
        binding.successGroup.isVisible = true
        Timber.d("Onsuccess ${data.size}")
        binding.emptyListGroup.isVisible = data.isEmpty()
        adapter.items = data
        adapter.notifyDataSetChanged()
    }

    override fun onError(throwable: Throwable) {
        binding.loadingBar.visibility = View.GONE
        showError(throwable.localizedMessage, binding.root)
    }
}
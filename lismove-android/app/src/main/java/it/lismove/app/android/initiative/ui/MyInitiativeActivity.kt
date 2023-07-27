package it.lismove.app.android.initiative.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityAccountConfigurationBinding
import it.lismove.app.android.databinding.ActivityMyInitiativeBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.adapter.SimpleStringAdapter
import it.lismove.app.android.general.adapter.data.SimpleItem
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.room.entity.EnrollmentEntity
import org.koin.android.ext.android.inject
import timber.log.Timber

class MyInitiativeActivity : LisMoveBaseActivity(), LceView<List<SimpleItem>> {
    lateinit var binding: ActivityMyInitiativeBinding
    val viewModel: MyInitiativeViewModel by inject()
    val adapter = SimpleStringAdapter(listOf()){
        openInitiativeDetail(viewModel.getEnrollment(it.id).enrollment)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyInitiativeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            initiativeRecyclerView.layoutManager = LinearLayoutManager(this@MyInitiativeActivity)
            initiativeRecyclerView.adapter = adapter
            addInitiativeButton.setOnClickListener { openNewInitiativeActivity() }
        }
        viewModel.stateObservable.observe(this, LceDispatcher(this))
        viewModel.loadInitiatives()
    }


    private fun openNewInitiativeActivity(){
        startActivity(Intent(this, RegistrationCodeActivity::class.java))
    }

    private fun openInitiativeDetail(enrollment: EnrollmentEntity){
        startActivity(InitiativeConfigurationActivity.getIntent(this, enrollment, false))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInitiatives()
    }

    override fun onLoading() {
        with(binding.loadingBar){
            isVisible = true
            isIndeterminate = true
        }
        with(binding){
            initiativeRecyclerView.isVisible = false
            noIitiativeLabel.isVisible = false
        }
    }

    override fun onSuccess(data: List<SimpleItem>) {
        with(binding){
            initiativeRecyclerView.isVisible = true
            noIitiativeLabel.isVisible = true
            binding.loadingBar.isVisible = false
            noIitiativeLabel.isVisible = data.isEmpty()


        }
        adapter.items = data
        adapter.notifyDataSetChanged()

    }

    override fun onError(throwable: Throwable) {
        binding.loadingBar.isVisible = false
        binding.initiativeRecyclerView.isVisible = true
        showError(throwable.localizedMessage ?: "Si è verificato un errore", binding.root)
    }
}
package it.lismove.app.android.session.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContentInfo
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import cn.pedant.SweetAlert.SweetAlertDialog
import it.lismove.app.android.databinding.ActivitySessionFeedBackBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.general.utils.PxDpConversionUtils
import it.lismove.app.android.session.data.FeedBackFormOption
import org.koin.android.ext.android.inject

class SessionFeedBackActivity : LisMoveBaseActivity(), LceView<List<FeedBackFormOption>> {
    lateinit var binding: ActivitySessionFeedBackBinding
    val viewModel: SessionFeedbackViewModel by inject()
    companion object{
        const val INTENT_SESSION_ID = "intent_session_id"
        fun getIntent(ctx: Context, sessionId: String): Intent{
            return Intent(ctx, SessionFeedBackActivity::class.java).apply {
                putExtra(INTENT_SESSION_ID, sessionId)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionFeedBackBinding.inflate(layoutInflater)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(binding.root)
        viewModel.optionsObservable.observe(this, LceDispatcher(this))
        val sessionId = intent.getStringExtra(INTENT_SESSION_ID) ?: ""
        viewModel.initViewModel(sessionId)
        viewModel.showLoading.observe(this){showLoadingAlert()}
        viewModel.showSuccess.observe(this){showSweetDialogSuccess("Segnalazione inviata con successo", "", {finish()})}
        viewModel.showError.observe(this){showSweetDialogError(it, {finish()})}
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onLoading() {
        with(binding){
            successLayout.isVisible = false
            progressBar.isVisible = true
            sendButton.isEnabled = false
        }
    }

    override fun onSuccess(data: List<FeedBackFormOption>) {
        with(binding){
            successLayout.isVisible = true
            progressBar.isVisible = false
            sendButton.isEnabled = true
            populateOptionList(data)
            sendButton.setOnClickListener { sendRequest() }

        }
    }

    private fun sendRequest() {
        var checkedIds = mutableListOf<Int>()
        var note = binding.noteEditText.text.toString()
        binding.checkLayout.children.forEach {
            (it as? CheckBox)?.let {
                if(it.isChecked){
                    checkedIds.add(it.tag as Int)
                }
            }
        }
        if(checkedIds.isEmpty()){
            showError("Seleziona almeno una tipologia di errore", binding.root)
        }else{
            viewModel.sendRequest(checkedIds, note)
        }

    }

    override fun onError(throwable: Throwable) {
        with(binding){
            successLayout.isVisible = false
            progressBar.isVisible = false
            sendButton.isVisible = true
        }
    }

    private fun populateOptionList(data: List<FeedBackFormOption>){
        with(binding){
            checkLayout.removeAllViews()
            getCheckBoxFromOptions(data).forEach { checkLayout.addView(it) }
        }
    }

    private fun getCheckBoxFromOptions(data: List<FeedBackFormOption>): List<CheckBox>{
        val params: LinearLayout.LayoutParams =
            LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        params.topMargin = PxDpConversionUtils.convertDpToPx(this, 8f).toInt()

        return data.map { option -> CheckBox(this@SessionFeedBackActivity).apply {
            text = option.label
            tag = option.id
            layoutParams = params
        } }
    }
}
package it.lismove.app.android.other

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zoho.livechat.android.ZohoLiveChat
import com.zoho.salesiqembed.ZohoSalesIQ
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityHelpAndFaqBinding
import org.koin.android.ext.android.inject
import androidx.appcompat.content.res.AppCompatResources

import com.zoho.commons.LauncherModes
import com.zoho.commons.LauncherProperties
import com.zoho.livechat.android.messaging.messenger.api.ZohoChatAPI
import it.lismove.app.android.chat.ChatManager
import it.lismove.app.android.chat.WhatsAppUtils


class HelpAndFaqActivity() : AppCompatActivity() {
    val viewModel: HelpAndFaqViewModel by inject()
    lateinit var binding: ActivityHelpAndFaqBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpAndFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupChat()
        with(binding.helpAndFaqWebView){
            loadUrl(viewModel.helpUrl)
        }

    }

    private fun setupChat(){
        if(viewModel.isChatEnabled){
          setupZohoChat()
        }else{
            setupWhatsappChat()
        }
    }

    private fun setupWhatsappChat() {
        with(binding){
            chatFab.visibility = View.VISIBLE
            chatFab.setOnClickListener {
                WhatsAppUtils.openDefaultChat(this@HelpAndFaqActivity)
            }
        }

    }

    private fun setupZohoChat(){
        with(binding){
            chatFab.visibility = View.GONE
            ZohoSalesIQ.showLauncher(true)
            chatFab.setOnClickListener {
                ZohoLiveChat.Chat.show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressed()
        return true
    }

    override fun onRestart() {
        super.onRestart()
        ZohoSalesIQ.showLauncher(true)

    }

    override fun onPause() {
        super.onPause()
        ZohoSalesIQ.showLauncher(false)
    }
}
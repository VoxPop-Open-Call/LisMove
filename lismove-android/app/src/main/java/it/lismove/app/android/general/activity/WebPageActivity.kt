package it.lismove.app.android.general.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.view.MenuItem
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.lismove.app.android.databinding.ActivityWebPageBinding

class WebPageActivity : AppCompatActivity() {
    lateinit var binding: ActivityWebPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initWebView()
        val title = intent.getStringExtra(INTENT_TITLE) ?: ""
        setTitle(title)
        getUrl(intent)?.let {
            loadUrl(it)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                super.onReceivedSslError(view, handler, error)

                handler?.cancel()
                Toast.makeText(this@WebPageActivity, "Il certificato SSL del sito è invalido. Riprova più tardi.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadUrl(pageUrl: String) {
        binding.webView.loadUrl(pageUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object{
        private const val INTENT_TITLE = "INTENT_TITLE"
        private const val INTENT_URL = "INTENT_URL"
        fun createIntent(url: String, context: Context, title: String = ""): Intent {
            return Intent(context, WebPageActivity::class.java).apply {
                putExtra(INTENT_URL, url)
                putExtra(INTENT_TITLE, title)
            }
        }

        private fun getUrl(intent: Intent) = intent.extras?.getString(INTENT_URL)

    }

}
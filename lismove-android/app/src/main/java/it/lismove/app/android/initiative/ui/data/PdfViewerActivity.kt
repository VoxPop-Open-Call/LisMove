package it.lismove.app.android.initiative.ui.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import es.voghdev.pdfviewpager.library.RemotePDFViewPager
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter
import es.voghdev.pdfviewpager.library.remote.DownloadFile
import es.voghdev.pdfviewpager.library.util.FileUtil
import it.lismove.app.android.databinding.ActivityPdfViewerBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import timber.log.Timber
import java.lang.Exception

class PdfViewerActivity : LisMoveBaseActivity(), DownloadFile.Listener {
    lateinit var binding: ActivityPdfViewerBinding
    private lateinit var pdfViewPager: RemotePDFViewPager

    companion object{
        val INTENT_PDF_URL = "Intent_pdf_url"
        val INTENT_TITLE = "INTENT_TITLE"
        fun getIntent(context: Context, pdfUrl: String, title: String = ""): Intent{
            return Intent(context, PdfViewerActivity::class.java).apply {
                putExtra(INTENT_PDF_URL, pdfUrl)
                putExtra(INTENT_TITLE, title)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(binding.root)
        intent.getStringExtra(INTENT_TITLE)?.let {
            title = it
        }
        intent.getStringExtra(INTENT_PDF_URL)?.let {
            pdfViewPager = RemotePDFViewPager(this, it, this)
        }

    }

    override fun onSuccess(url: String?, destinationPath: String?) {
        pdfViewPager.adapter = PDFPagerAdapter(this, FileUtil.extractFileNameFromURL(url))
        binding.pdfView.addView(pdfViewPager,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onFailure(e: Exception?) {
        Timber.e("Exception with download $e")
        showError(e?.localizedMessage ?: "", binding.root)
    }

    override fun onProgressUpdate(progress: Int, total: Int) {
        Timber.d("onProgressUpdate: $progress / $total")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        (pdfViewPager.adapter as PDFPagerAdapter).close()
    }
}
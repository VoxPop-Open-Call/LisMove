package it.lismove.app.android.other

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import timber.log.Timber

object UriUtils {
    fun openUri(url: String, ctx: Context) {
        try {
            val uri = Uri.parse(url)
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            ContextCompat.startActivity(ctx, browserIntent, null)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            Toast.makeText(ctx, "Nessun browser installato per aprire il link.", Toast.LENGTH_SHORT).show()
        }
    }
}
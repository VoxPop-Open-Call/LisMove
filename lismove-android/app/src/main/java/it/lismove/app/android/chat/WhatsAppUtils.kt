package it.lismove.app.android.chat

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppUtils {
    private const val phoneNumber = "393401642396"
    fun openDefaultChat(ctx: Context){
        val uri: Uri =
            Uri.parse("https://wa.me/$phoneNumber")
        //Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + "")

        val sendIntent = Intent(Intent.ACTION_VIEW, uri)

        ctx.startActivity(sendIntent)
    }
}
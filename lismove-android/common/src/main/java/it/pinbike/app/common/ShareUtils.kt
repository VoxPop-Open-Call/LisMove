package it.lismove.app.common

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun saveAttachmentAndOpenShareIntent(title: String, receiverEmail: String, mailBody: String = "", attachmentText: String? = null, fileName: String? = null, activity: AppCompatActivity) {
        val logFile: File? = save(attachmentText, fileName, activity)

        if (logFile != null) {
            ShareCompat.IntentBuilder.from(activity)
                .setType("message/rfc822")
                .addEmailTo(receiverEmail)
                .setSubject(title)
                .setText(mailBody)
                .setStream(FileProvider.getUriForFile(activity, activity.packageName + ".fileprovider", logFile))
                .setChooserTitle("Email")
                .startChooser()
        } else {
            ShareCompat.IntentBuilder.from(activity)
                .setType("message/rfc822")
                .addEmailTo(receiverEmail)
                .setSubject(title)
                .setText(mailBody)
                .setChooserTitle("Email")
                .startChooser()
        }
    }

    fun save(content: String?, fileName: String?, ctx: Context): File? {
        if (content == null) return null

        val outFile = File(ctx.getExternalFilesDirs(null)[0], fileName)

        if (outFile.exists()) {
            outFile.delete()
            outFile.createNewFile()
        } else {
            outFile.createNewFile()
        }

        val out = FileOutputStream(outFile, true)
        out.write(content.toByteArray())

        out.flush()
        out.close()

        return outFile
    }
}
package it.lismove.app.android.general.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import it.lismove.app.android.session.data.Session
import it.lismove.app.common.DateTimeUtils
import java.io.File
import java.io.FileOutputStream

object GpxUtils {

    fun buildGpxAndShare(session: Session, ctx: Context) {
        val gpxData = gpxFromSession(session)
        saveAndShowShareIntent(gpxData, ctx)
    }

    fun gpxFromSession(session: Session): String {
        val sessionName = "Sessione del ${DateTimeUtils.getReadableDateTime(session.startTime)}"
        val xmlBuilder = StringBuilder()
            with (xmlBuilder) {
                append(getHeader())
                append(getTrackStart(sessionName))
                session.partials
                    .filter { it.latitude == 0.0 && it.longitude == 0.0 }
                    .forEach {
                    append(getTrackPoint(
                        it.latitude, it.longitude, it.altitude,
                        DateTimeUtils.getReadableDateTime(it.timestamp)
                    ))
                }
                append(getTrackEnd())
                append(getFooter())
            }

        return xmlBuilder.toString()
    }

    private fun getTrackPoint(
        lat: Double?,
        lng: Double?,
        ele: Double?,
        time: String,
    ) = """
        <trkpt lat="$lat" lon="$lng"><ele>$ele</ele><time>$time</time></trkpt>
    """.trimIndent()

    private fun getHeader(
        creator: String = "LisMove",
        time: String = DateTimeUtils.getReadableDateTime(System.currentTimeMillis())
    ) = """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx
         version="1.0"
        creator="$creator"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.topografix.com/GPX/1/0"
        xsi:schemaLocation="http://www.topografix.com/GPX/1/0
        http://www.topografix.com/GPX/1/0/gpx.xsd">
        <time>$time</time>
    """.trimIndent()

    private fun getTrackStart(
        name: String,
    ) = """
        <trk>
            <name>$name</name>
            <number>1</number>
            <trkseg>
    """.trimIndent()

    private fun getTrackEnd() = "</trkseg></trk>"
    private fun getFooter() = "</gpx>"

    fun saveAndShowShareIntent(data: String, ctx: Context) {
        val logFile = saveOnDisk(data, ctx)

        val i = Intent(Intent.ACTION_SEND)
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setType("application/gpx+xml")
            .putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", logFile))
        try {
            ctx.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(ctx, "No app found that supports this file type", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun saveOnDisk(data: String, ctx: Context): File {
        with (File(ctx.getExternalFilesDirs(null)[0], "session-${System.currentTimeMillis()}.gpx")){
            if (exists()) {
                delete()
                createNewFile()
            } else {
                createNewFile()
            }

            with (FileOutputStream(this, true)){
                write(data.toByteArray())
                flush()
                close()
            }

            return this
        }
    }
}
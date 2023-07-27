package it.lismove.app.common

import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalTime
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import org.joda.time.format.DateTimeFormat




object DateTimeUtils {

    fun dateIsInFuture(date: Long): Boolean{
        return date.minus(getCurrentTimestamp()) > 0
    }
    fun getCurrentTimestamp(): Long{
        return DateTime.now().millis
    }
    fun getCurrentDate(): Date{
        return DateTime.now().toDate()
    }

    fun getTodayAtMidnight(): Long {
        return DateTime.now().withTime(LocalTime.MIDNIGHT).millis
    }


    fun daysPassed(date: Long): Int{
        val now = DateTime.now()
        val activationDate = DateTime(date)
        return Days.daysBetween(activationDate, now).days
    }

    fun daysUntil(date: Long): Int{
        val now = DateTime.now().withTimeAtStartOfDay()
        val evalDate = DateTime(date)
        return Days.daysBetween(evalDate, now).days
    }

    fun getTimeStampFromDateFormatted(date: String?): Long?{
        if(date.isNullOrEmpty()){
            return null
        }else{
            try {
                Timber.d("getTimeStampFromDateFormatted $date")
                val df = SimpleDateFormat("d/M/yyyy", Locale.ITALY)
                df.timeZone = TimeZone.getTimeZone("UTC")
                val parseDate = df.parse(date)
                return parseDate?.time
            }catch (e: Exception){
                return null
            }
        }

    }

    fun getReadableShortDate(timestamp: Long?): String {
        if (timestamp == null) return "No date available"

        val date = Date(timestamp)
        val sdf = SimpleDateFormat("d/MM/yyyy", Locale.ITALIAN)
        return sdf.format(date)
    }

    fun getReadableDateTime(timestamp: Long?): String {
        if (timestamp == null) return "No date available"

        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.ITALIAN)
        return sdf.format(date)
    }

    fun getReadableDate(timestamp: Long?): String {
        if (timestamp == null) return "No date available"

        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.ITALIAN)
        return sdf.format(date)
    }

    fun getReadableMonthYear(dateTimeString: String): String {
        val pattern = "yyyy-MM-dd'T'HH:mm:ss"
        val dtf = DateTimeFormat.forPattern(pattern)
        val dateTime = dtf.parseDateTime(dateTimeString)
        val month = dateTime.monthOfYear().asShortText
        val year = dateTime.yearOfCentury().asShortText
        return "$month/$year"
    }

    fun getDayAsTimestamp(dateTimeString: String) : Long {
        val pattern = "yyyy-MM-dd'T'HH:mm:ss"
        val dtf = DateTimeFormat.forPattern(pattern)
        val dateTime = dtf.parseDateTime(dateTimeString)

        return dateTime.millis
    }

    fun getFilterString(date: Date): String{
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }
    fun getStringTime(timestamp: Long) = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ITALIAN).format(this)
}
package it.lismove.app.room.entity

import android.text.format.DateUtils
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.format.DateTimeFormat

import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern

import java.util.*
@Entity
data class SettingsEntity(
    @PrimaryKey val organizationId: Long,
    val isActiveUrbanPoints: Boolean = true,
    val startDateUrbanPoints: String? = null,
    val endDateUrbanPoints: String? = null,
    val startDateBonus: String? = null,
    val endDateBonus: String? = null,
    val endTimeBonus: String? = null,
    val startTimeBonus: String? = null,
    val multiplier: Int = 1,
    val isActiveTimeSlotBonus: Boolean = false,
    val exclusiveCustomField: Boolean = false,
    val ibanRequirement: Boolean = false,
    val initiativeRefund: Boolean = false,
    val homeWorkRefund: Boolean = false
){
    fun getActiveMultiplier(date: Date): Int{
        return if(hasActiveBonusInDate(date)) multiplier else 1
    }

    //ERRATA
    private fun hasActiveBonusInDate(date: Date): Boolean{
        return if (startDateBonus != null && endDateBonus != null){
            val startDate = getDateFromString(startDateBonus, null)
            val endDate = getDateFromString(endDateBonus, null)
            if(isActiveTimeSlotBonus.not()) return !(date.before(startDate) || date.after(endDate))
            else{
                val pattern =  DateTimeFormat.forPattern("yyyy-MM-dd")
                val startDate = getDateFromString(pattern.print(date.time), startTimeBonus)
                val endDate = getDateFromString(pattern.print(date.time), endTimeBonus)
                return !(date.before(startDate) || date.after(endDate))
            }
        }else{
            false
        }
    }


    private fun getDateFromString(date: String, time: String?): Date?{
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val timeOrMidnight = time ?: "00:00"
        return formatter.parse("${date}T$timeOrMidnight")
    }

    private fun getShortDateFromString(date: String): String{
        getDateFromString(date, null)?.let {
            val sdf = SimpleDateFormat("dd MMMM", Locale.getDefault())
            return sdf.format(it)
        }
        throw Exception("Error parsing date")
    }
    fun getBonusString(initiativeName: String): String{
        if(startDateBonus !=  null && endDateBonus != null){
            var basic =  "Dal ${getShortDateFromString(startDateBonus)} al ${getShortDateFromString(endDateBonus)} i punti accumulati nell'iniziativa $initiativeName sono x$multiplier"
            if(isActiveTimeSlotBonus && startTimeBonus != null && endTimeBonus != null){
                basic += " se guadagnati dalle ore $startTimeBonus e le $endTimeBonus"
            }
            return basic
        }else{
            return "Nessun bonus attivo"
        }
    }
}
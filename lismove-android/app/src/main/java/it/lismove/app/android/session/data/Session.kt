package it.lismove.app.android.session.data

import java.util.concurrent.TimeUnit

class Session(
    val id: String? = null,
    val uid: String = "",

    /* Populated by SDK */
    val startBattery: Int? = null, //ok
    val endBattery: Int? = null, //ok
    val firmwareVersion: String? = null,

    val phoneStartBattery: Int? = null,  //ok
    val phoneEndBattery: Int? = null,  //ok

    val startTime: Long = 0L, //ok
    val endTime: Long = 0L, //ok

    val gyroDistance: Double? = null, //ok
    val partials: List<Partial> = listOf(), //ok

    val totalKm: Double= 0.0, //deprecated
    val description: String? = null, //ok

    /* Populated by SERVER */
    val type: Int? = null, //ok
    var polyline: List<String>? = null,  //ok
    //val urbanKm: Int? = null, //deprecated
    //val urbanPoints: Int? = null, //deprecated

    // nationalKm null se la sessione non è stata inviata
    // se non è null, mostrare nationalKm come distanza percorsa
    val nationalKm: Double? = null,
    val nationalPoints: Int? = null, //ok
    var sessionPoints: List<SessionPoint> = listOf(),  //ok
    val valid: Boolean? = null, //ok

    val euro: Double? = null, //ok
    val certificated: Boolean = false, //ok
    val homeWorkPath: Boolean? = null, //ok
    //val multiplier: Int? = null, //deprecated
    val status: Int? = null, //ok

    val uploaded: Boolean = true,
    val co2: Double? = null,
    val gmapsDistance: Double? = 0.0,
    val gpsOnlyDistance: Double? = 0.0,
    // Duration in Seconds
    val duration: Long = 0L,
    var verificationRequired: Boolean? = null,
    var verificationRequiredNote: String? = null
    ){
    fun getReadableElapsedTime(): String{
        return "%02d:%02d:%02d".format(
            TimeUnit.SECONDS.toHours(duration),
            TimeUnit.SECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(duration)),
            TimeUnit.SECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration)))
    }

    fun getAvgSpeedReadable() : String{
        val avgSpeed = if(duration == 0L) {
            0.0
        }else{
            val durationInHours: Double = duration.toDouble()/3600.0
            val distanceInKm = getDistance()
            // Km/h
            distanceInKm/durationInHours
        }

        return "%.2f".format(avgSpeed)
    }
    fun getDistanceReadable() = "%.2f".format(getDistance())

    fun getValidatedNationalPoints(): Int{
        val nationalPoints =  nationalPoints ?: 0
        return if(valid == true || uploaded.not()) nationalPoints else 0

    }


    fun getDistance(): Double{
        return nationalKm ?: (gyroDistance ?: 0.0) + (gpsOnlyDistance ?: 0.0)
    }

    fun computeInitiativePoints(): Int {
        var points = 0
        sessionPoints.forEach { points += it.points }
        return points
    }

    fun getValidatedTotalInitiativePoints(): Int{
        return if(valid == true || uploaded.not()) computeInitiativePoints() else 0
    }
    fun getValidatedInitiativePoints(): List<SessionPoint>{
        return if(valid == true || uploaded.not()) sessionPoints else sessionPoints.map  {it.copy(points = 0)}
    }
    fun getValidatedInitiativeNumber(): Int{
        return if(valid == true || uploaded.not()) sessionPoints.size else 0
    }
    fun getReadableStatusMessage(): String?{
        return when(status){
            0 -> "Sessione verificata"
            1 -> "Sessione corrotta"
            2 -> "Distanza non accurata"
            3 -> "Velocità non valida"
            4 -> "Sessione verificata"
            5 -> "Non abbastanza distanza certificata dal sensore"
            6 -> "Debug"
            7 -> "Accelerazione non valida"
            8 -> "Sessione verificata"
            else -> null
        }

    }

}
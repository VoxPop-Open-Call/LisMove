package it.lismove.app.android.session.data


data class SessionPoint (
    val distance: Double,
    val multiplier: Double,
    val organizationId: Long,
    val points: Int,
    val sessionId: String? = null,
    val euro: Double? = null,
    var refundStatus: Int? = null,

){
    @Transient
    var hasRefundEnabled: Boolean = false //DO NOT use unless in SessionDetail
    @Transient
    var organizationName: String = "" //DO NOT use unless in SessionDetail
    fun getRefundStatus(): String{
        return when(refundStatus){
            0 -> "Sessione nazionale"
            1 -> "L'intero importo è stato riconosciuto"
            2 -> "Parzialmente riconosciuto per raggiungimento soglia giornaliera"
            3 -> "Parzialmente riconosciuto per raggiungimento soglia mensile"
            4 -> "Parzialmente riconosciuto per raggiungimento soglia iniziativa"
            5 -> "Non riconosciuto per raggiungimento soglia giornaliera"
            6 -> "Non riconosciuto per raggiungimento soglia mensile"
            7 -> "Non riconosciuto per raggiungimento soglia iniziativa"
            else -> ""

        }
    }
}
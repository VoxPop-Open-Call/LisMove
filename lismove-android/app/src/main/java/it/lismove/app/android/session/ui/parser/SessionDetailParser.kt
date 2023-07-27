package it.lismove.app.android.session.ui.parser

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import it.lismove.app.android.R
import it.lismove.app.android.dashboard.data.RankingPointData
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.ui.data.SessionDetailUI
import it.lismove.app.android.session.ui.data.TotalPoints
import it.lismove.app.room.entity.SettingsEntity


object SessionDetailParser {
    fun getSessionDetail(session: Session,
                         totalNationalPoint: RankingPointData?,
                         totalInitiativePoints: List<RankingPointData>?,
                         isFromHistory: Boolean
    ): SessionDetailUI {

        var polyline: List<List<LatLng>>? = null
        var totalPoints: TotalPoints? = null
        lateinit var errorMessage: String
        var messageIcon = if(session.status == 0 || session.status == 8 || session.status == 4)  R.drawable.ic_baseline_done_24 else R.drawable.ic_baseline_error_outline_24
        var messageColor = if(session.status == 0 || session.status == 8 || session.status == 4) R.color.GreenColor2 else R.color.AccentColor


        fun populateErrorMessage(session: Session){
            errorMessage = if (session.uploaded) session.getReadableStatusMessage() ?: "" else "Sessione non inviata"
        }

        fun getPointsFromEncodedString(pointsEncoded: List<String>): List<List<LatLng>>{
            return pointsEncoded.map { PolyUtil.decode(it) }
        }

        fun populatePolyline(session: Session){
            session.polyline?.let {
                polyline = if(session.polyline.isNullOrEmpty()) null else getPointsFromEncodedString(it)
            }
        }

        fun populateTotalPointsIfNotFromHistory(
            isFromHistory: Boolean,
            totalInitiativePoints: List<RankingPointData>?,
            totalNationalPoint: RankingPointData?
        ){
            if(isFromHistory.not() && totalInitiativePoints != null && totalNationalPoint != null){
                totalPoints = TotalPoints(
                    totalInitiativeNumber = totalInitiativePoints.size,
                    totalPointsInitiative = totalInitiativePoints,
                    totalPointsNational = totalNationalPoint.points.toString()
                )
            }
        }

        populateErrorMessage(session)
        populatePolyline(session)
        populateTotalPointsIfNotFromHistory(isFromHistory, totalInitiativePoints, totalNationalPoint)

        var showRefundLayout = session.sessionPoints.any { it.hasRefundEnabled } && session.certificated
        val refundLabel = session.sessionPoints.filter { it.hasRefundEnabled }.map { "${it.organizationName}: ${it.getRefundStatus()}"}.reduceRightOrNull { s, acc ->  "$acc\n\n$s"} ?: ""
        return SessionDetailUI(
                session.getDistanceReadable(),
                session.getReadableElapsedTime(),
                session.getAvgSpeedReadable(),
                sessionPointsNational =  session.getValidatedNationalPoints().toString(),
                sessionInitiativeNumber = session.getValidatedInitiativePoints().size,
                totalPoints = totalPoints,
                date =   DateTimeUtils.getReadableDateTime(session.startTime),
                polyline =  polyline,
                showInfoMessage =  true,
                message = errorMessage,
                id = session.id ?: "",
                isInSyncWithServer = session.uploaded,
                showInitiativePoints = session.getValidatedInitiativePoints().size < 2 || session.getValidatedTotalInitiativePoints() == 0,
                sessionInitiativePoints = session.getValidatedTotalInitiativePoints().toString(),
                showZeroPointLabel = session.getValidatedTotalInitiativePoints() == 0,
                validationRequired = session.verificationRequired,
                messageIcon = messageIcon,
                messageIconTint = messageColor,
                showRefundLayout = showRefundLayout,
                refundEuro = "${session.euro ?: 0.0}",
                refundLabel = refundLabel
        )
    }
}
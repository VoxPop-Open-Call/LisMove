package it.lismove.app.android.session.ui.data

import com.google.android.gms.maps.model.LatLng
import it.lismove.app.android.dashboard.data.RankingPointData

data class SessionDetailUI(
    val distance: String,
    val duration: String,
    val speed: String,
    val sessionInitiativeNumber: Int?,
    val sessionPointsNational: String?,
    val totalPoints: TotalPoints?,
    val date: String,
    val polyline: List<List<LatLng>>? = null,
    val showInfoMessage: Boolean = true,
    val message: String = "",
    val messageIcon: Int,
    val messageIconTint: Int,
    val id: String,
    val isInSyncWithServer: Boolean,
    val showInitiativePoints: Boolean,
    val showZeroPointLabel: Boolean,
    val sessionInitiativePoints: String = "",
    val validationRequired: Boolean? = null,
    val showRefundLayout: Boolean = false,
    val refundEuro: String = "",
    val refundLabel: String = ""
    )

data class TotalPoints(
    val totalInitiativeNumber: Int?,
    val totalPointsNational: String,
    val totalPointsInitiative: List<RankingPointData>
)
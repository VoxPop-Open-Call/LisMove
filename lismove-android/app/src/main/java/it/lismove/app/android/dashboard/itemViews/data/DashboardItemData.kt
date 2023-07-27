package it.lismove.app.android.dashboard.itemViews.data

import it.lismove.app.android.dashboard.data.ActiveInitiativeData
import it.lismove.app.android.dashboard.data.RankingPointData

sealed class DashboardItemData(open val title: String,
                               open val alertDescription: String?,
                               open var pos: Int,
                               open val type: Int,
                               open val errorString: String? = null,
                               open val isLoading: Boolean,
                               open val stretched: Boolean = false,
                               open val onCardClicked: ()->Unit = {}
)

data class SimpleTextDashboardItemData(
    override val title: String = "",
    override val alertDescription: String? = null,
    override var pos: Int,
    override val type: Int,
    override val stretched: Boolean = false,
    override val errorString: String? = null,
    override val isLoading: Boolean = false,
    val mainText: String = "",
    val leftSmallText: String? = null,
    val leftIconRes: Int? = null,
    override val onCardClicked: ()->Unit = {}
): DashboardItemData(title, alertDescription, pos, type, errorString, stretched,isLoading, onCardClicked, )

data class ImageListDashboardItemData(
    override val title: String,
    override val alertDescription: String? = null,
    override var pos: Int,
    override val type: Int,
    override val stretched: Boolean = true,
    override val errorString: String? = null,
    override val isLoading: Boolean = false,
    val emptyImagesText: String ,
    val images: List<ActiveInitiativeData> = listOf(),
    override val onCardClicked: ()->Unit = {}
): DashboardItemData(title, alertDescription, pos, type, errorString, stretched, isLoading, onCardClicked)

data class ProfileDashboardItemData(
    override val title: String = "",
    override val alertDescription: String? = null,
    override var pos: Int,
    override val type: Int,
    override val stretched: Boolean = false,
    override val errorString: String? = null,
    override val isLoading: Boolean = false,
    val text: String = "",
    val image: String? = null,
    val avgLeftText: String = "",
    val avgRightText: String = "",
    override val onCardClicked: ()->Unit = {}
): DashboardItemData(title, alertDescription, pos, type, errorString, stretched, isLoading,onCardClicked)

data class RankingDashboardItemData(
    override val title: String,
    override val alertDescription: String?,
    override var pos: Int,
    override val type: Int,
    override val stretched: Boolean,
    override val errorString: String? = null,
    override val isLoading: Boolean,
    val rankings: List<RankingPointData>,
    override val onCardClicked: ()->Unit = {}

): DashboardItemData(title, alertDescription, pos, type, errorString, stretched,isLoading, onCardClicked)

data class ChartDashboardItemData(
    override val title: String,
    override val alertDescription: String?,
    override var pos: Int,
    override val type: Int,
    override val stretched: Boolean,
    override val errorString: String? = null,
    override val isLoading: Boolean,
    val points: List<ChartPointData>,
    override val onCardClicked: ()->Unit = {}
): DashboardItemData(title, alertDescription, pos, type, errorString, stretched, isLoading, onCardClicked)

data class SensorListDashboardItemData(
    override val title: String,
    override val alertDescription: String?,
    override var pos: Int,
    override val type: Int,
    override val stretched: Boolean,
    override val errorString: String? = null,
    override val isLoading: Boolean,
    var sensorListData: SensorListData,
    override val onCardClicked: ()->Unit = {}
): DashboardItemData(title, alertDescription, pos, type,errorString, stretched, isLoading, onCardClicked)

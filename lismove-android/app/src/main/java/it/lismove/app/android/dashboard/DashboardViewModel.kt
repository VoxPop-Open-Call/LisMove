package it.lismove.app.android.dashboard

import android.content.Context
import androidx.lifecycle.*
import it.lismove.app.android.R
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.dashboard.data.ActiveInitiativeData
import it.lismove.app.android.dashboard.data.RankingPointData
import it.lismove.app.android.dashboard.data.UserDashboardResponse
import it.lismove.app.android.dashboard.itemViews.DashBoardItem
import it.lismove.app.android.dashboard.itemViews.data.*
import it.lismove.app.android.dashboard.parser.asChartPointDataList
import it.lismove.app.android.dashboard.repository.DashboardRepository
import it.lismove.app.android.dashboard.useCases.TotalPointsUseCase
import it.lismove.app.android.deviceConfiguration.repository.SensorRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.general.utils.getDecimalPart
import it.lismove.app.android.general.utils.getIntegerPart
import it.lismove.app.room.entity.DashoardPositionEntity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import it.lismove.app.room.entity.SensorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lv.chi.photopicker.utils.SingleLiveEvent
import net.nextome.lismove_sdk.LismoveSensorSdk
import net.nextome.lismove_sdk.models.LisMoveBleState
import net.nextome.lismove_sdk.utils.BugsnagUtils
import kotlin.Exception

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val userRepository: UserRepository,
    private val totalPointsUseCase: TotalPointsUseCase,
    private val sensorRepository: SensorRepository,
    private val sensorSdk: LismoveSensorSdk,
    val user: LisMoveUser
): ViewModel() {
    var dashBoard: Lce<UserDashboardResponse> = LceLoading()
    var initiatives: Lce<List<EnrollmentWithOrganization>> = LceLoading()
    var points: Lce<List<RankingPointData>> =  LceLoading()
    var sensorList: Lce<List<SensorEntity>> =  LceLoading()
    var sensorListItem: Lce<List<SensorItemData>> =  LceLoading()
    var itemPositions: List<DashoardPositionEntity> = arrayListOf()
    var deviceNotFoundCounter = 0

    val deviceNotFoundObserver = MutableLiveData<String>()
    val errorObserver = SingleLiveEvent<Throwable>()
    val dashboardState: MutableStateFlow<Lce<List<DashboardItemData>>> = MutableStateFlow(LceSuccess(listOf()))
    val dashboardUpdateObservable = SingleLiveEvent<Int>()
    lateinit var dashboardCallback: DashboardCallback

    var items = arrayListOf<DashboardItemData>()

    fun getDashboardData(context: Context, dashboardCallback: DashboardCallback){
        this.dashboardCallback = dashboardCallback

         viewModelScope.launch {
             initDashboardItems()
             updateDashBoardData()
             updateInitiativeData()
             updateSensorData(context)
         }
    }

    fun updateSensorData(context: Context) {
        viewModelScope.launch {
            val sensor = sensorRepository.getSensor(user.uid)
            if (sensor != null){
                sensorList = LceSuccess(listOf(sensor))
                sensorListItem = LceSuccess(listOf(SensorItemData(sensorName = sensor.name, sensorConnected = false)))
            }else{
                sensorList = LceSuccess(listOf())
                sensorList = LceSuccess(listOf())
            }

            updateItems(listOf(DashoardPositionEntity.SENSOR))
            observeSensorStatus(context)
        }
    }

    private fun updateInitiativeData() {
        viewModelScope.launch {
            initiatives = LceSuccess(userRepository.getActiveInitiatives(user.uid))
            updateItems(listOf(DashoardPositionEntity.PROJECTS))
            updateRankings()
        }
    }

    private fun updateRankings() {
        viewModelScope.launch {
            var updatedPoints = arrayListOf<RankingPointData>()
            try {
                updatedPoints .add(totalPointsUseCase.getTotalNationalPoints(user))
                initiatives.data?.forEach {
                    updatedPoints .add(RankingPointData(it.organization.notificationLogo, it.enrollment.points ?: 0,  it.organization.id,"Progetto ${it.organization.title}"))
                }
                points = LceSuccess(updatedPoints)
            }catch (e: Exception){
                points = LceError(e)
            }

            updateItems(listOf(DashoardPositionEntity.POINTS))
        }
    }

    private fun updateDashBoardData() {
        viewModelScope.launch {
            dashBoard = try{
                LceSuccess(dashboardRepository.getDashboard(user.uid))
            }catch (e: Exception){
                LceError(e)
            }
            updateItems(listOf(
                DashoardPositionEntity.PROFILE,
                DashoardPositionEntity.KM_DONE,
                DashoardPositionEntity.CO2,
                DashoardPositionEntity.USAGE,
                DashoardPositionEntity.EUROS,
                DashoardPositionEntity.MESSAGES
            ))
        }
    }

    private fun updateItems(itemsType: List<Int>) {
        items = arrayListOf()
        createInitialItems(positions = itemPositions)
        itemsType.forEach {  type ->
            val itemPosition = itemPositions.firstOrNull { it.dashboardItemId == type }

            if (itemPosition != null) {
                dashboardUpdateObservable.value = itemPosition.dashboardPosition
            } else {
                // BugsnagUtils.reportIssue(Exception("Item position was null in Update Items"))
            }
        }

    }

    private suspend fun initDashboardItems(): List<DashboardItemData>{
        items = arrayListOf()
        dashboardState.emit(LceLoading())
        itemPositions = dashboardRepository.getDashboardItemPositions()
        createInitialItems(itemPositions)
        dashboardState.emit(LceSuccess(items))
        return items
    }

    private fun createInitialItems(positions: List<DashoardPositionEntity>) {
        positions.forEach {
            when(it.dashboardItemId){
                DashoardPositionEntity.PROFILE -> addProfileItem(it.dashboardPosition, user, dashBoard)
                DashoardPositionEntity.CO2 -> addCo2Item(it.dashboardPosition, dashBoard)
                DashoardPositionEntity.KM_DONE -> addKmItem(it.dashboardPosition, dashBoard)
                DashoardPositionEntity.SENSOR -> addSensorItem(it.dashboardPosition)
                DashoardPositionEntity.EUROS -> addEurosItem(it.dashboardPosition, dashBoard)
                DashoardPositionEntity.POINTS -> addPointsItem(it.dashboardPosition, points)
                DashoardPositionEntity.USAGE -> addUsageItem(it.dashboardPosition, dashBoard)
                DashoardPositionEntity.PROJECTS -> addProjectsItem(it.dashboardPosition, initiatives)
                DashoardPositionEntity.MESSAGES -> addMessagesItem(it.dashboardPosition, dashBoard)
            }
        }
    }

    private fun addMessagesItem(dashboardPosition: Int, dashBoard: Lce<UserDashboardResponse>) {
        items.add(SimpleTextDashboardItemData(
            title = "Nuovi messaggi",
            alertDescription = null,
            pos = dashboardPosition,
            type = DashoardPositionEntity.MESSAGES,
            mainText = "${dashBoard.data?.messages ?: 0}",
            errorString = dashBoard.error?.message,
            stretched = false,
            isLoading = dashBoard.loading,
            leftIconRes = R.drawable.ic_baseline_mail_outline_24,
            leftSmallText = "",
            onCardClicked = { dashboardCallback.onOpenNotification() }
        ))
    }

    private fun addProjectsItem(
        dashboardPosition: Int,
        initiatives: Lce<List<EnrollmentWithOrganization>>
    ) {
        items.add(ImageListDashboardItemData(
            title = "Iniziative attive (clicca per info)",
            alertDescription = null,
            pos = dashboardPosition,
            type = DashoardPositionEntity.PROJECTS,
            emptyImagesText = "Nessuna iniziativa attiva",
            isLoading = initiatives.loading,
            errorString = initiatives.error?.message,
            images = initiatives.data?.map {
                ActiveInitiativeData(
                    title = it.organization.title,
                    imageRes = it.organization.initiativeLogo,
                    initiativeRule = it.organization.getSanitizedRegulation()
                )
            } ?: listOf(),
            onCardClicked = {
                dashboardCallback.onCardClicked(DashoardPositionEntity.PROJECTS)
            }
        ))
    }

    private fun addUsageItem(dashboardPosition: Int, dashBoard: Lce<UserDashboardResponse>) {
        items.add(ChartDashboardItemData(
            title = "Utilizzo giornaliero",
            alertDescription = null,
            pos = dashboardPosition,
            type = DashoardPositionEntity.USAGE,
            stretched = true,
            errorString = dashBoard.error?.message,
            isLoading = dashBoard.loading,
            points =  dashBoard.data?.dailyDistance?.asChartPointDataList() ?: listOf(),
            onCardClicked = {
                dashboardCallback.onCardClicked(DashoardPositionEntity.USAGE)
            }
        ))
    }

    private fun addPointsItem(dashboardPosition: Int, points: Lce<List<RankingPointData>>) {
        items.add(RankingDashboardItemData(
            title = "Totale punti",
            stretched = true,
            isLoading = points.loading,
            errorString = points.error?.message,
            rankings = points.data ?: listOf(),
            alertDescription = null,
            pos = dashboardPosition,
            type = DashoardPositionEntity.POINTS,
            onCardClicked = {
                dashboardCallback.onCardClicked(DashoardPositionEntity.POINTS)
            }
        ))
    }

    private fun addEurosItem(dashboardPosition: Int, dashBoard: Lce<UserDashboardResponse>) {
        items.add(SimpleTextDashboardItemData(
            title = "Euro ricevuti",
            alertDescription = null,
            pos = dashboardPosition,
            type = DashoardPositionEntity.EUROS,
            isLoading = dashBoard.loading,
            errorString = dashBoard.error?.message,
            mainText = dashBoard.data?.euro.getIntegerPart(),
            leftSmallText = dashBoard.data?.euro.getDecimalPart("€"),
            onCardClicked = {
                dashboardCallback.onCardClicked(DashoardPositionEntity.EUROS)
            }

        ))
    }

    private fun addSensorItem(dashboardPosition: Int) {
        items.add(SensorListDashboardItemData(
            title = "Dispositivi associati",
            alertDescription = null,
            pos = dashboardPosition,
            type = DashoardPositionEntity.SENSOR,
            stretched = true,
            isLoading = sensorList.loading,
            errorString = sensorList.error?.message,
            sensorListData = SensorListData(false, sensorListItem.data ?: listOf()) { dashboardCallback.onRefreshDeviceRequested() },
            onCardClicked = {
                dashboardCallback.onCardClicked(DashoardPositionEntity.SENSOR)
            }
        ))
    }

    private fun addKmItem(dashboardPosition: Int, dashBoard: Lce<UserDashboardResponse>) {
        items.add(SimpleTextDashboardItemData(
            title = "Strada percorsa",
            alertDescription = null,
            pos = dashboardPosition,
            type = DashoardPositionEntity.KM_DONE,
            isLoading = dashBoard.loading,
            errorString = dashBoard.error?.message,
            mainText = dashBoard.data?.distance.getIntegerPart(),
            leftSmallText = dashBoard.data?.distance.getDecimalPart("km"),
            onCardClicked = {
                dashboardCallback.onCardClicked(DashoardPositionEntity.KM_DONE)
            }
        ))
    }

    private fun addCo2Item(dashboardPosition: Int, dashBoard: Lce<UserDashboardResponse>) {
        items.add(SimpleTextDashboardItemData(
            title = "C02 risparmiata",
            alertDescription = null,
            pos = dashboardPosition,
            type = DashoardPositionEntity.CO2,
            isLoading = dashBoard.loading,
            errorString = dashBoard.error?.message,
            mainText = dashBoard.data?.co2?.div(1000).getIntegerPart(),
            leftSmallText = dashBoard.data?.co2?.div(1000).getDecimalPart("g"),
            onCardClicked = {
                dashboardCallback.onCardClicked(DashoardPositionEntity.CO2)
            }
            ))
    }

    private fun addProfileItem(
        position: Int,
        user: LisMoveUser,
        dashboard: Lce<UserDashboardResponse>
    ) {
        items.add(ProfileDashboardItemData(
            type = DashoardPositionEntity.PROFILE,
            pos = position,
            isLoading = dashboard.loading,
            errorString = dashBoard.error?.message,
            title = user.username ?: "",
            image = user.avatarUrl,
            text = dashboard.data?.sessionNumber?.toString() ?: "",
            avgLeftText = dashboard.data?.sessionDistanceAvg?.getIntegerPart() ?: "0",
            avgRightText = dashboard.data?.sessionDistanceAvg?.getDecimalPart("km") ?: ".00 km",
            onCardClicked = {
                dashboardCallback.onCardClicked(DashoardPositionEntity.PROFILE)
            }
        ))
    }

    fun updatePosition(items: List<DashBoardItem>){
       viewModelScope.launch {
            itemPositions = items.map { DashoardPositionEntity(it.data.type, it.data.pos) }
            this@DashboardViewModel.items = ArrayList(items.map { it.data })
            dashboardRepository.updateDashboardItemPositions(itemPositions)
        }
    }

    fun requestDeviceRefresh(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        if (isConnected) {
            return@launch
        }

        sensorRepository.getSensor(user.uid)?.let {
            sensorSdk.scanAndConnectToSensorIfNecessary(it.uuid, it.wheelDiameter.toFloat(), it.hubCoefficient)
        }


        withContext(Dispatchers.Main) {
            // Start loading data
            val sensorCard = items.firstOrNull { it.type == DashoardPositionEntity.SENSOR } as SensorListDashboardItemData?
                ?: return@withContext

            sensorCard.sensorListData = sensorCard.sensorListData.copy(isLoading = true)
            dashboardUpdateObservable.value = sensorCard.pos

            delay(6000L)
            if (!isConnected) {
                sensorCard.sensorListData = sensorCard.sensorListData.copy(isLoading = false)
                dashboardUpdateObservable.value = sensorCard.pos

                deviceNotFoundObserver.value = context.getString(R.string.sensor_not_detected)
                deviceNotFoundCounter++
            } else {
                deviceNotFoundCounter = 0
            }
        }
    }

    var isConnected = false
    private fun observeSensorStatus(context: Context){
        viewModelScope.launch {

            sensorListItem = LceSuccess(sensorList.data?.map { sensor ->
                return@map SensorItemData(sensorName = sensor.name, sensorConnected = false)
            } ?: listOf())

            sensorSdk.observeBleStatus().collect { sensorState ->
                try {
                    val sensorCard = items.firstOrNull { it.type == DashoardPositionEntity.SENSOR} as SensorListDashboardItemData?
                    if (sensorCard != null) {
                        sensorCard.sensorListData = sensorCard.sensorListData.copy(isLoading = true)
                        dashboardUpdateObservable.value = sensorCard.pos
                    } else {
                        // BugsnagUtils.reportIssue(Exception("sensorCard was null in observeSensorStatus"))
                    }

                    when (sensorState) {
                        LisMoveBleState.BLE_CONNECTING -> {}
                        LisMoveBleState.BLE_CONNECTED -> {}
                        LisMoveBleState.BLE_READY -> {
                            isConnected = true
                            sensorListItem = LceSuccess(sensorList.data?.map { sensor ->
                                return@map SensorItemData(sensorName = sensor.name, sensorConnected = true)
                            } ?: listOf())

                        }
                        LisMoveBleState.BLE_DISCONNECTING -> {}
                        LisMoveBleState.BLE_DISCONNECTED -> {
                            isConnected = false

                            sensorListItem = LceSuccess(sensorList.data?.map { sensor ->
                                return@map SensorItemData(sensorName = sensor.name, sensorConnected = false)
                            } ?: listOf())
                        }

                        LisMoveBleState.BLE_FAILED_TO_CONNECT -> {
                        }
                    }

                    updateItems(listOf(DashoardPositionEntity.SENSOR))

                }catch (e: Exception){
                    errorObserver.value = e
                }
            }
        }
    }
}
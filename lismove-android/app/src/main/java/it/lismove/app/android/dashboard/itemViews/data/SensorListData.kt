package it.lismove.app.android.dashboard.itemViews.data

data class SensorListData (
    val isLoading: Boolean,
    val sensorList: List<SensorItemData>,
    val onRefreshRequested: ()-> Unit
)
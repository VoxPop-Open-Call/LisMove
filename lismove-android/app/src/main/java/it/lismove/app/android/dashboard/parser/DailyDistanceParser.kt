package it.lismove.app.android.dashboard.parser

import it.lismove.app.android.dashboard.data.UserDistanceStats
import it.lismove.app.android.dashboard.itemViews.data.ChartPointData


fun List<UserDistanceStats>.asChartPointDataList(): List<ChartPointData>{
    return sortedBy { it.getDayAsTimestamp() }
        .mapIndexed { index, userDistanceStats -> userDistanceStats.asChartPointData(index)
    }
}

fun UserDistanceStats.asChartPointData(index: Int): ChartPointData{
    return ChartPointData(
        index.toFloat(),
        distance.toFloat(),
        getReadableDay()
    )
}

package it.lismove.app.android.dashboard.data

import it.lismove.app.common.DateTimeUtils
import net.nextome.lismove_sdk.utils.BugsnagUtils

data class UserDashboardResponse (
    val co2: Double = 0.0,
    val distance: Double = 0.0,
    val euro: Double = 0.0,
    val sessionNumber: Int = 0,
    val dailyDistance: List<UserDistanceStats> = listOf(),
    val messages: Int = 0,
    val sessionDistanceAvg: Double = 0.0
) {

}

data class UserDistanceStats(
    val day: String,
    val distance: Double
){
    fun getReadableDay(): String{
        try {
            return DateTimeUtils.getReadableMonthYear(day)
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
            return "---/--"
        }
    }

    fun getDayAsTimestamp(): Long {
        try {
            return DateTimeUtils.getDayAsTimestamp(day)
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
            return 0
        }
    }
}
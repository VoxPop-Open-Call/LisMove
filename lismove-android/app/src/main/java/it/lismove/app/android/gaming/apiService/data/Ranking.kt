package it.lismove.app.android.gaming.apiService.data

import it.lismove.app.common.DateTimeUtils


data class Ranking(
    val endDate: Long?,
    val filter: Long?,
    val filterValue: String?,
    val id: Long,
    val organization: Long?,
    val rankingPositions: List<LismoverRank>? = null,
    val startDate: Long?,
    val title: String,
    val value: Int? = null,
    val priority: Int = 0
){
    var organizationName: String? = null
    var organizationNotificationImage: String? = null
    var organizaitonRegulation: String? = null

    fun getUserRanking(username: String): LismoverRank?{
        return rankingPositions?.firstOrNull{it.username.equals(username)}
    }

    fun getValueLabel(): String{
        return if(value != null && value < value_label.size){
            value_label[value]
        }else{
            "punti community"
        }
    }

    fun getDateIntervalLabel(): String?{
        return  if(startDate == null && endDate == null) {
            null
        }else if(endDate != null){
                "Dal ${DateTimeUtils.getReadableShortDate(startDate)} al ${DateTimeUtils.getReadableShortDate(endDate)}"
            }else{
                "Dal ${DateTimeUtils.getReadableDate(startDate)} "
            }

    }

    companion object{
        val URBAN_KM = 0
        val WORK_KM = 1
        val WORK_NUM = 2
        val INITIATIVE_POINTS = 3
        val NATIONAL_POINTS = 4
        val NATIONAL_KM = 5

        val value_label = listOf<String>(
            "km iniziativa",
            "km casa/lavoro",
            "sessioni casa/lavoro",
            "punti iniziativa",
            "punti community",
            "km community"
        )
    }

}
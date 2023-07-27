package it.lismove.app.android.gaming.apiService.data

data class Achievement (
    val duration: Int,
    val filter: Int,
    val filterValue: String,
    val organizationTitle: String,
    val fullfilled: Boolean,
    val endDate: Long?,
    val id: Long,
    val logo: String?,
    val name: String,
    val organization: Long,
    val score: Double = 0.0,
    val target: Double,
    val user: String,
    val value: Int
){
    fun getLabelForType(): String{
        return when(value){
            VALUE_URBAN_KM -> "km iniziativa"
            VALUE_WORK_KM -> "km casa/lavoro"
            VALUE_WORK_NUM -> "sessioni casa/lavoro"
            VALUE_INITIATIVE_POINTS -> "punti iniziativa"
            VALUE_NATIONAL_KM -> "km community"
            else -> "km"
        }
    }

    companion object{
        val VALUE_URBAN_KM = 0
        val VALUE_WORK_KM = 1
        val VALUE_WORK_NUM = 2
        val VALUE_INITIATIVE_POINTS = 3
        val VALUE_NATIONAL_POINTS = 4
        val VALUE_NATIONAL_KM = 5

    }
}

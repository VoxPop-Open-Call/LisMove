package it.lismove.app.android.awards.data

data class AwardAchievement(
    val achievement: Long,
    val description: String,
    val id: Long,
    val imageUrl: String?,
    val name: String = "",
    val type: Int,
    val value: Double
){
    fun getTypeLabel(): String{
        return when(type){
            TYPE_MONEY -> "euro"
            TYPE_POINTS -> "punti"
            else -> throw Exception("Type differs from expected")
        }
    }
    companion object{
        const val TYPE_MONEY = 0
        const val TYPE_POINTS = 1
    }
}

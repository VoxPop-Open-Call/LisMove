package it.lismove.app.android.awards.data

data class AwardRanking (
    val description: String?,
    val id: Long,
    val imageUrl: String?,
    val name: String,
    val position: Int,
    val range: String,
    val ranking: Long,
    val type: Int,
    val value: Float
){
    fun getTypeLabel(): String{
        return when(type){
            TYPE_MONEY -> "euro"
            TYPE_POINTS -> "punti"
            else -> throw Exception("Type differs from expected")
        }
    }
    companion object{
        val TYPE_MONEY = 0
        val TYPE_POINTS = 1
    }
}


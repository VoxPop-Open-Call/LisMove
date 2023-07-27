package it.lismove.app.android.car.data

data class CarModification(
    val co2: String,
    val engineDisplacement: Int?,
    val fuel: String,
    val fuelConsumptionExtraurban: String,
    val fuelConsumptionUrban: String,
    val generation: Long,
    val id: Long
){
    fun getModificationDescription(): String{
        return "$fuel ${getEngineDisplacementString()}"
    }

    private fun getEngineDisplacementString(): String{
        var res = ""
        engineDisplacement?.let {
            res+= "($engineDisplacement)"
        }
        return res
    }
}

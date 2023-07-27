package it.lismove.app.android.car.data

data class CarGenerationExpanded(
    val id: Long,
    val model: CarModelExpanded,
    val modelYear: Int,
    val name: String
)
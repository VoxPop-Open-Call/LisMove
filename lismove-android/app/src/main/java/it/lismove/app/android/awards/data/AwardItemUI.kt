package it.lismove.app.android.awards.data

data class AwardItemUI (
    val id: String,
    val image: String?,
    val name: String,
    val rightIcon: Int? = null,
    val rightText: String? = null,
    val rightElementsColor: Int? = null,
    val header: String? = null,
    val value: String? = null,
    val valueType: String? = null
)
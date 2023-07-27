package it.lismove.app.android.initiative.ui.data

data class ListAlertData(
    val id: String? = null,
    val leftText: String,
    var rightText: String,
    val topText: String? = null,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val center: Boolean = false,
    val bottomText: String? = null,

    )
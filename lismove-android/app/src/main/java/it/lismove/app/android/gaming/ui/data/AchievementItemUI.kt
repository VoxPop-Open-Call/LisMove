package it.lismove.app.android.gaming.ui.data

data class AchievementItemUI (
    val id: String,
    val title: String,
    val projectName: String?,
    val percentageValue: Int,
    val percentageString: String,
    val imageUrl: String?,
    val fulfilled: Boolean,
    val daysCounter: String? = null
)
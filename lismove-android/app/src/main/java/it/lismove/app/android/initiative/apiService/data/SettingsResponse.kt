package it.lismove.app.android.initiative.apiService.data

data class SettingsResponse(
    val id: Int,
    val value: String,
    val defaultValue: String,
    val organizationSetting: String,
    val organization: Int
)


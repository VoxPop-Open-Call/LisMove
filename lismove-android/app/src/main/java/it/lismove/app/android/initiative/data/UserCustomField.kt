package it.lismove.app.android.initiative.data

data class UserCustomField(
    val customFieldId: Long,
    val description: String?,
    val name: String?,
    val organization: Long,
    val type: Int,
    var value: Boolean,
    var eid: Long
)
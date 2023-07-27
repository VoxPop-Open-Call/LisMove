package it.lismove.app.android.session.apiService

data class SessionValidationRequest (
    var id: String,
    var verificationRequired: Boolean,
    var verificationRequiredNote: String?,
    var revisionType: List<Int>,
    var verificationRequiredExtra: String?,
)

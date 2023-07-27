package it.lismove.app.android.authentication.useCases.data


sealed class LoginState(
    val logged: Boolean = false,
    open val userId: String? = null,
    open val error: Throwable? = null)

class LoginLoggedSuccess(override val userId: String): LoginState(true, userId, null)
class LoginLoggedProfileIncomplete(override val userId: String): LoginState(true, userId, null)
class LoginTermsNotAccepted(override val userId: String): LoginState(true, userId, null)
class LoginUnLogged(): LoginState(false, null, null)
class LoginLoggedBlocked(override val userId: String): LoginState(false, userId, null)
class LoginEmailNotVerified(override val userId: String): LoginState(true, userId, null)
class CachedUserExpired(override val userId: String): LoginState(true, userId, null)

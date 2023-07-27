package it.lismove.app.android.authentication.ui.data

sealed class EmailSignInState(
    open val emailError: String? = null,
    open val passwordError: String?  = null,
    open val genericError: String? = null,
    open val loggedIn: Boolean = false,
    val loading: Boolean = false,
){

    class InitialState(): EmailSignInState(null, null, null, false)
    class InputFieldError(override val passwordError: String?, override val emailError: String?): EmailSignInState(emailError, passwordError, null, false)
    class GenericError(override val genericError: String?): EmailSignInState(null, null, genericError, false)
    class LoginSuccess(override val loggedIn: Boolean) :  EmailSignInState(null, null, null, loggedIn)
    class Loading() : EmailSignInState(loading = true)
    class ChangePasswordRequired(): EmailSignInState()
}



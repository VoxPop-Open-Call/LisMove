package it.lismove.app.android.general.network

import it.lismove.app.android.general.utils.fromJson
import net.nextome.lismove_sdk.utils.BugsnagUtils
import timber.log.Timber
import java.io.IOException
import java.lang.Exception

class LismoveNetworkException(val serializedResponse: String?): IOException() {
    var status: Int = 0
    var error: String = ""
    var errorMessage: String = ""

    init {
        try {
            val errorBody = serializedResponse?.fromJson<LismoveNetworkErrorBody>()

            if (errorBody != null) {
                if(status == 500){
                    Timber.d("error is 500 ${errorBody.message}")
                    errorMessage = "Si è verificato un errore temporaneo"
                }else{
                    errorMessage = errorBody.message
                }
                status = errorBody.status
                error = errorBody.error

            }
        }catch (e: Exception){
            BugsnagUtils.reportIssue(Exception("Network Error: $serializedResponse"))
            status = -1
            errorMessage = "Si è verificato un errore temporaneo"
        }

    }

    override val message: String
        get() = errorMessage

    override fun getLocalizedMessage(): String? {
        return errorMessage
    }

    data class LismoveNetworkErrorBody(
        val status: Int,
        val error: String,
        val message: String,
    )
}

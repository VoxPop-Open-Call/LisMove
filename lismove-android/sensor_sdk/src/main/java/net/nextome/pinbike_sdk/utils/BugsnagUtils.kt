package net.nextome.lismove_sdk.utils

import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Severity

object BugsnagUtils {
    enum class ErrorSeverity { INFO, WARNING, ERROR }

    fun setUser(userId: String?, email: String?, name: String?){
        Bugsnag.setUser(userId, email, name)
    }

    fun logEvent(message: String) {
        Bugsnag.leaveBreadcrumb(message)
    }

    fun reportIssue(exception: Throwable, severity: ErrorSeverity = ErrorSeverity.ERROR) {
        Bugsnag.notify(exception) { event ->
            event.severity = when (severity) {
                ErrorSeverity.INFO -> Severity.INFO
                ErrorSeverity.WARNING -> Severity.WARNING
                ErrorSeverity.ERROR -> Severity.ERROR
            }

            true
        }
    }

    fun reportWorkerIssue(exception: Throwable, sessionId: String?, macAddress: String?, deviceName: String?) {
        Bugsnag.notify(exception) {
            with (it) {
                addMetadata("service", "sessionId", sessionId)
                addMetadata(
                    "service",
                    "device_mac",
                    macAddress
                )
                addMetadata(
                    "service",
                    "device_name",
                    deviceName
                )

                severity = Severity.ERROR
            }

            true
        }
    }

}
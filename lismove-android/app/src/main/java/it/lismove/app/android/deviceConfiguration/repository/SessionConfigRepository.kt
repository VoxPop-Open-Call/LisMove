package it.lismove.app.android.deviceConfiguration.repository


import it.lismove.app.android.deviceConfiguration.SessionConfig

interface SessionConfigRepository {
    fun saveSessionConfig(config: SessionConfig)
    fun loadSessionConfig(): SessionConfig?
    fun clearSessionConfig()
}
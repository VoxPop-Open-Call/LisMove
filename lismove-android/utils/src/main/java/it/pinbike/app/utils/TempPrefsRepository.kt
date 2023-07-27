package it.lismove.app.utils

import it.lismove.app.room.entity.LisMoveUser


interface TempPrefsRepository {
    fun saveTempUser(user: LisMoveUser)
    fun getTempUser(): LisMoveUser

    fun setConfigSent(value: Boolean)
    fun isConfigSent(): Boolean

    fun setOptimizationSent(value: Boolean)
    fun isOptimizationSent(): Boolean

    fun setSessionFloatingOpen(value: Boolean)
    fun isSessionFloatingOpen(): Boolean
}
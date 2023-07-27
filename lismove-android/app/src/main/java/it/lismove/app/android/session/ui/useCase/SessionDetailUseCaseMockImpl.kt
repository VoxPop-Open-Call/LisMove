package it.lismove.app.android.session.ui.useCase

import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.data.SessionPoint


class SessionDetailUseCaseMockImpl: SessionDetailUseCase{
    lateinit var session: Session
    override suspend fun getSession(id: String, uid: String): Session {
        session =  Session(
            id = "de3b3efc-2cee-4570-9043-622270b2f3c9",
            uid = "testUid",
            type = 0,
            valid = false,
            status = 2,
            startTime = 1636035868483,
            endTime = 1636035895591,
            duration = 21,
            gyroDistance = 0.20033,
            gpsOnlyDistance = 0.0,
            gmapsDistance = 0.0,
            totalKm = 0.3,
            nationalPoints = 0,
            description = "Sessione mockata",
            startBattery = 86,
            endBattery = 86,
            polyline = listOf("g`oyFga`fBN?Dl@vBE?{AFKh@CAsA?u@CyAsAAeAACdBCbAi@BCB?B"),
            phoneStartBattery = 60,
            phoneEndBattery = 10,
            homeWorkPath = false,
            sessionPoints = listOf(
                SessionPoint(23.0, 1.0, 1, 0),
                SessionPoint(12.0, 1.0, 1, 0),
            )
        )
        return session
    }


    override suspend fun getSessionPoints(sessionId: String,  uid: String): List<ListAlertData> {
        return listOf(
            ListAlertData("test", "Org", "0"),
            ListAlertData("test", "Org 2", "0")
        )
    }

    override suspend fun requestVerification(sessionId: String, userId: String, reason: String, types: List<Int>): Session {
        session.verificationRequired = true
        session.verificationRequiredNote = reason
        return session
    }

    override suspend fun requestPointVerification(
        sessionId: String,
        userId: String,
        reason: String
    ): Session {
        session.verificationRequired = true
        session.verificationRequiredNote = reason
        return session
    }

    override suspend fun getSessionWithPartials(sessionId: String): Session {
        return session
    }

}
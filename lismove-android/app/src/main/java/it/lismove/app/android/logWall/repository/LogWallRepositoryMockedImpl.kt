package it.lismove.app.android.logWall.repository

import kotlinx.coroutines.delay

class LogWallRepositoryMockedImpl: LogWallRepository {
    var counter = 1
    override suspend fun getLogWallEvents(): List<String> {
        delay(200)
        counter ++
        val flag = if(counter > 6)  6 else counter
        return listOf(
            "Fabio69 ha appena completato una sessione di 50km e ha guadagnato 10 euro",
            "Fabio69 ha appena completato una sessione di 50km e ha guadagnato 20 euro",
            "Fabio50 ha appena completato una sessione di 50km e ha guadagnato 20 euro",
            "UserTester ha appena completato una sessione di 56km e ha guadagnato 20 euro",
            "User ha appena completato una sessione di 50km e ha guadagnato 20 euro",
            "Tester ha appena completato una sessione di 3km e ha guadagnato 20 euro",
            "Testera ha appena completato una sessione di 56km e ha guadagnato 20 euro"
        ).subList(0, flag)
    }
}
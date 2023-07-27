package it.lismove.app.android.session.data

data class SessionDashBoardData(
        val time: String = "--:--:--",
        val distance: String = "0.00",
        val avgSpeed: String = "0 ",
        val speed: String = "0",
        val nationalPoints: String = "0",
        val initiativePoints: String = "0",
        val sensorBatteryIcon: Int? = null,
        val isSensorBatteryAvailable: Boolean = true,
        val isGps: Boolean = false,
        val activeInitiatives: Int = 0,
        val urban: Boolean = false,
        val multiplierValue: String = "x1",
        val multiplierLabelEnd: String = "(x0)",
        val showMultiplier: Boolean = false


)

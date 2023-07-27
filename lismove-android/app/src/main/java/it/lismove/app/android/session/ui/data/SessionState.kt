package it.lismove.app.android.session.ui.data

import it.lismove.app.android.session.data.SessionDashBoardData


sealed class SessionState()

object SessionStateInitial: SessionState()

object SessionStateNone: SessionState()

class SessionStatePaused(val lastSessionData: SessionDashBoardData): SessionState()

class SessionStateStopped(val lastSessionData: SessionDashBoardData): SessionState()

object SessionLoading: SessionState()

class SessionStateStarted(val updatedSessionData: SessionDashBoardData): SessionState()

class SessionUploaded(val sessionId: String): SessionState()

class ShowSensorConfigurationPopup(): SessionState()

class SessionShowErrorDialog(val title: String, val message: String): SessionState()

class SessionUploadError(val title: String, val message: String, val sessionId: String): SessionState()

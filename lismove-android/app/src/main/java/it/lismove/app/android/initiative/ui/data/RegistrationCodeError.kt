package it.lismove.app.android.initiative.ui.data

data class CodeEmptyError(
    override val message: String = "Inserisci un codice valido"
): Error()

data class CodeIncorrectError(
    override val message: String = "Codice iniziativa non valido"
): Error()

data class EnrollmentFinished(
    override val message: String = "L'iniziativa è ormai conclusa"
): Error()
package it.lismove.app.android.authentication.useCases.data


data class DataIncompleteError(
        val nameError: String,
        val surnameError: String,
        val nicknameError: String,
        val addressError: String,
        val numberError: String,
        val cityError: String,
        val lismoveCityError: String,
        val dateError: String,
        val ibanError: String
): Error()
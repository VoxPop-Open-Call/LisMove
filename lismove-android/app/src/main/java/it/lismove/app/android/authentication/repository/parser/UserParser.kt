package it.lismove.app.android.authentication.repository.parser

import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.LisMoveUserEntity
import it.lismove.app.room.entity.SeatEntity

fun LisMoveUserEntity.asLisMoveUser(workAddresses: List<SeatEntity> = listOf()): LisMoveUser {
    return LisMoveUser(
        uid = uid,
        firstName = firstName,
        lastName = lastName,
        username = username,
        email = email,
        homeAddress = homeAddress,
        homeNumber = homeNumber,
        homeCity = homeCity,
        homeCityExtended = homeCityExtended,
        birthDate = birthDate,
        gender = gender,
        avatarUrl = avatarUrl,
        emailVerified = emailVerified,
        termsAccepted = termsAccepted,
        marketingTermsAccepted = marketingTermsAccepted,
        signupCompleted = signupCompleted,
        activePhone = activePhone,
        phoneActivationTime = phoneActivationTime,
        lastLoggedIn = lastLoggedIn,
        workAddresses = workAddresses,
        activePhoneModel = activePhoneModel,
        activePhoneToken = activePhoneToken,
        activePhoneVersion = activePhoneVersion,
        iban = iban
    )
}

fun LisMoveUser.asLisMoveUserEntity(): LisMoveUserEntity{
    return LisMoveUserEntity(
        uid = uid,
        firstName = firstName,
        lastName = lastName,
        username = username,
        email = email,
        homeAddress = homeAddress,
        homeNumber = homeNumber,
        homeCity = homeCity,
        homeCityExtended = homeCityExtended,
        birthDate = birthDate,
        gender = gender,
        avatarUrl = avatarUrl,
        emailVerified = emailVerified,
        termsAccepted = termsAccepted,
        marketingTermsAccepted = marketingTermsAccepted,
        signupCompleted = signupCompleted,
        activePhone = activePhone,
        phoneActivationTime = phoneActivationTime,
        lastLoggedIn = lastLoggedIn,
        activePhoneModel = activePhoneModel,
        activePhoneToken = activePhoneToken,
        activePhoneVersion = activePhoneVersion,
        iban = iban
    )
}
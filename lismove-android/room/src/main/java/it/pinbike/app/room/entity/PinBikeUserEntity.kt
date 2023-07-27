package it.lismove.app.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LisMoveUserEntity(
    @PrimaryKey val uid: String,
    var firstName: String? = null,
    var lastName: String? = null,
    val username: String? = null,
    val email: String,
    var homeAddress: String? = null,
    var homeNumber: String? = null,
    var homeCity: Int? = null,
    @Embedded var homeCityExtended: LisMoveCityEntity? = null,
    val birthDate: Long? = null, //Timestamp in milliseconds
    var gender: String? = null,
    var avatarUrl: String? = null,
    val emailVerified: Boolean = false,
    val termsAccepted: Boolean = false,
    val marketingTermsAccepted: Boolean = false,
    val signupCompleted: Boolean = false,
    var activePhone: String? = null,
    var phoneActivationTime: Long? = null,
    var lastLoggedIn: Long? = null,
    var activePhoneToken: String? = null,
    var activePhoneModel: String? = null,
    var activePhoneVersion: String? = null,
    var iban: String? = null

    ){



}
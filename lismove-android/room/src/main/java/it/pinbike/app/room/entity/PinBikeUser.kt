package it.lismove.app.room.entity

import timber.log.Timber


data class LisMoveUser(
    val uid: String,
    var firstName: String? = null,
    var lastName: String? = null,
    val username: String? = null,
    val email: String,
    var homeAddress: String? = null,
    var homeNumber: String? = null,
    var homeCity: Int? = null,
    var homeCityExtended: LisMoveCityEntity? = null,
    val birthDate: Long? = null, //Timestamp in milliseconds
    var gender: String? = null,
    var avatarUrl: String? = null,
    var homeLatitude: Double? = null,
    var homeLongitude: Double? = null,
    var emailVerified: Boolean = false,
    val termsAccepted: Boolean = false,
    val marketingTermsAccepted: Boolean = false,
    var signupCompleted: Boolean = false,
    var resetPasswordRequired: Boolean? = false,
    var activePhone: String? = null,
    var phoneActivationTime: Long? = null,
    var lastLoggedIn: Long? = null,
    var workAddresses: List<SeatEntity>? = listOf(),
    var phoneNumber: String? = null,
    var activePhoneToken: String? = null,
    var activePhoneModel: String? = null,
    var activePhoneVersion: String? = null,
    var iban: String? = null
){

    //TODO: Check home address
    fun isProfileComplete(): Boolean{
        val flag = !firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && !username.isNullOrEmpty()
        isHomeAddressComplete() && (birthDate != null) && isIbanNullOrCorrect()
        return flag
    }

    fun isHomeAddressComplete(): Boolean{
        val hasAll = (!homeAddress.isNullOrEmpty()).and(homeCity != null)
        return hasAll
    }

    fun isHomeAddressNullOrComplete(): Boolean{
        val hasNone = homeAddress.isNullOrEmpty().and(homeCity == null)
        val hasAll = (!homeAddress.isNullOrEmpty()).and(homeCity != null)
        Timber.d("IsUserAddressValid ${hasAll || hasNone}")
        return hasAll || hasNone
    }


    fun isIbanNullOrCorrect(): Boolean{
        if(iban.isNullOrEmpty()){
            return true
        }else{
            return iban?.isValidIban() ?: true
        }
    }
}
fun String.isValidIban(): Boolean {

    if (!"^[0-9A-Z]*\$".toRegex().matches(this)) {
        return false
    }

    val symbols = this.trim { it <= ' ' }
    if (symbols.length < 15 || symbols.length > 34) {
        return false
    }
    val swapped = symbols.substring(4) + symbols.substring(0, 4)
    return swapped.toCharArray()
        .map { it.code }
        .fold(0) { previousMod: Int, _char: Int ->
            val value = Integer.parseInt(_char.toChar().toString(), 36)
            val factor = if (value < 10) 10 else 100
            (factor * previousMod + value) % 97
        } == 1

}
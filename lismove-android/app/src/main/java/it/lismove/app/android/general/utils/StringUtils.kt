package it.lismove.app.android.general.utils

import android.text.TextUtils
import android.text.util.Linkify
import android.util.Patterns
import timber.log.Timber

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.toBearerToken(): String{
    return "Bearer $this"
}

fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()

fun String?.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    pat.toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this?: "")
        .map { it.range.first }
        .toList()

 fun String.extractUrl(): String? =
    this
        .split(" ", "\n")
        .firstOrNull { Patterns.WEB_URL.matcher(it).find() }?.removeSuffix(".")

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
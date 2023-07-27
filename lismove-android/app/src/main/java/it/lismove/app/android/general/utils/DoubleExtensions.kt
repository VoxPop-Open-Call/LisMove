package it.lismove.app.android.general.utils

import java.text.DecimalFormatSymbols

private fun Double.getComponents(): List<String>{
    return String.format("%.2f", this).split(DecimalFormatSymbols.getInstance().decimalSeparator)
}

fun Double?.getIntegerPart(): String{
    if (this == null) return "0"
    return this.getComponents().first()
}

fun Double?.getDecimalPart(measurement: String = ""): String {
    if (this == null) return ".00 $measurement"
    return ".${getComponents().last()} $measurement"
}
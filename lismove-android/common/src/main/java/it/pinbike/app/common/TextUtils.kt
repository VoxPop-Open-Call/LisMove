package it.lismove.app.common

fun String.sanitizeHtmlText() = replace("\u2028", "\n")
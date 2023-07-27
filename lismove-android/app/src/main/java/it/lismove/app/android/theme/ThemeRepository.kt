package it.lismove.app.android.theme

interface ThemeRepository {
    fun getTheme(): Int
    fun setTheme(theme: Int)
    fun resetTheme()
}
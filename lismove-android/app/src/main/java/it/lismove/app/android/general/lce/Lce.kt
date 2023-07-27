package it.lismove.app.android.general.lce

// Abstract class with restricted class hierarchies
// Only the class defined in the same file can extend this class
sealed class Lce<TYPE>(
    val loading: Boolean = false,
    open val data: TYPE? = null,
    open val error: Throwable? = null)

class LceLoading<TYPE> : Lce<TYPE>(true, null, null)
data class LceSuccess<TYPE>(override val data: TYPE) : Lce<TYPE>(false, data, null)
data class LceError<TYPE>(override val error: Throwable) : Lce<TYPE>(false, null, error)
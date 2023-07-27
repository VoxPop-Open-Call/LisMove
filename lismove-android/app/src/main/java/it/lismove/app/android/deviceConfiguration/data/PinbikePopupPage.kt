package it.lismove.app.android.deviceConfiguration.data

data class LismovePopupPage(
    val title: String,
    var description: String,
    val image: Int? = null,
    val buttonText: String? = null,
    val showEditText: Boolean = false,
    val showLoading: Boolean = false,
    val topImage: Int? = null,
    val bottomImage: Int? = null,
)


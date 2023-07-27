package it.lismove.app.android.awards.data

data class Coupon (
    val code: String,
    val emissionDate: Long,
    val expireDate: Long?,
    val organizationId: Long?,
    val redemptionDate: Long?,
    val refundDate: Long?,
    val title: String?,
    val uid: String?,
    val value: Double,
    val shopName: String?,
    val shopLogo: String?,
    val articleImage: String?,
    val articleTitle: String?
) {
    val isRedeemed: Boolean
        get() = redemptionDate != null || refundDate != null
}
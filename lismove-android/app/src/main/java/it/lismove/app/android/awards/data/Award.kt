package it.lismove.app.android.awards.data

data class Award(
    val description: String? = null,
    val imageUrl: String? = null,
    val name: String = "",
    val organizationId: Long? = null, //TODO: organizationName
    val timestamp: Long? = null,
    val type: Int = 0,
    val value: Double? = 0.0,

    val achievementId: Long? = null,

    val address: String? = null,
    val city: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val number: Int? = null,
    val radius: Double? = null,
    val uid: String? = null,
    val username: String? = null,

    val range: String? = null,
    val position: Int? = null,
    val rankingId: Long? = null,

    val coupon: Coupon?

){
    val refundOrderValue: Int
        get() {
            if(awardType == AwardType.SHOP ||awardType == AwardType.TOWN_HALL){
                if(coupon?.redemptionDate == null) return 1 else return 0
            }else{
                return 0
            }
        }
    val awardType: AwardType
        get() {
            return AwardType.fromInt(type)
        }
    val awardValueLabel: String?
        get() {
            when(awardType){
                AwardType.EURO -> return "euro"
                AwardType.POINTS -> return "punti"
                AwardType.TOWN_HALL -> return null
                AwardType.SHOP -> return "punti"
            }
        }
}
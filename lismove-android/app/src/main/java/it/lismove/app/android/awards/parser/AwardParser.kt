package it.lismove.app.android.awards.parser

import it.lismove.app.android.R
import it.lismove.app.android.awards.data.Award
import it.lismove.app.android.awards.data.AwardItemUI
import it.lismove.app.android.awards.data.AwardType
import it.lismove.app.android.awards.data.Coupon

fun Award.asAwardItemUI(position: Int): AwardItemUI{
    return AwardItemUI(
        id = position.toString(),
        image = imageUrl,
        name = name,
        value = value?.toString(),
        valueType = if(value != null) awardValueLabel else null,
        rightElementsColor = this.getRightColor(),
        rightIcon = getRightIconImage(this),
        rightText = getRightText(this),
    )
}


private fun getRightIconImage(award: Award): Int?{
    when(award.awardType){
        AwardType.EURO, AwardType.SHOP, AwardType.TOWN_HALL ->  return getRedeemedImage(award.coupon)
        else -> return null
    }
}

private fun getRedeemedImage(coupon: Coupon?): Int{
    return if(coupon?.isRedeemed == true){
        R.drawable.ic_ticket_done
    }else{
        R.drawable.ic_ticket_base
    }
}
fun Award.getRightColor(): Int? {
    return when (this.awardType) {
        AwardType.EURO -> getRedeemColor(this)
        AwardType.SHOP, AwardType.TOWN_HALL -> getRefundColor(this)
        else -> return null
    }
}
fun getRedeemColor(award: Award): Int{
    if(award.coupon?.redemptionDate != null){
        return R.color.gray_image_tint
    }else{
        return R.color.red_main
    }
}

fun getRefundColor(award: Award): Int{
    if(award.coupon?.refundDate != null){
        return R.color.gray_image_tint
    }else{
        return R.color.green
    }
}
private fun getRightText(award: Award): String?{
    return when(award.awardType){
        AwardType.EURO -> getEuroRightString(award)
        AwardType.SHOP, AwardType.TOWN_HALL ->  getRedeemRightString(award)
        else -> return null
    }

}

fun getEuroRightString(award: Award): String{
    if(award.coupon?.refundDate != null){
        return "RIMBORSATO"
    }else{
        return "DA\nRIMBORSARE"
    }
}


fun getRedeemRightString(award: Award): String{
    if(award.coupon?.redemptionDate != null){
        return "RISCATTATO"
    }else{
        return "DA\nRISCATTARE"
    }
}
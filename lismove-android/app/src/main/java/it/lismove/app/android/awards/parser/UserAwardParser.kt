package it.lismove.app.android.awards.parser

import it.lismove.app.android.R
import it.lismove.app.android.awards.AwardDetailUI
import it.lismove.app.android.awards.data.Award
import it.lismove.app.android.awards.data.AwardType
import it.lismove.app.common.DateTimeUtils

fun Award.asAwardDetailUI(): AwardDetailUI{
    return when(awardType){
        AwardType.EURO -> this.asEuroAward()
        AwardType.POINTS -> this.asPointsAward()
        AwardType.TOWN_HALL -> this.asTownHallAward()
        AwardType.SHOP -> this.asShopAward()
        else -> throw Exception("Award Type not found")
    }
}

fun Award.asEuroAward(): AwardDetailUI{
    return AwardDetailUI(
        imageUrl = imageUrl,
        title = name,
        description = description ?: "",
        valueLabel = "Euro",
        value = value.toString(),
        header = null,
        emissionDate = if(timestamp != null) DateTimeUtils.getReadableDate(timestamp) else null,

        hasCoupon = true,

        state = if(coupon?.refundDate != null) "RIMBORSATO" else "DA RIMBORSARE",
        stateColor = this.getRightColor(),

        refundLabel = if(coupon?.refundDate != null) "Data emissione bonifico" else null,
        refundDate = if(coupon?.refundDate != null) DateTimeUtils.getReadableDate(coupon.refundDate) else null,

        refundType = "Bonifico in conto corrente"


    )
}

fun Award.asPointsAward(): AwardDetailUI{
    return AwardDetailUI(
        imageUrl = imageUrl,
        title = name,
        description = description ?: "",
        valueLabel = "Punti",
        value = value?.toString(),
        header = null,
        emissionDate = if(timestamp != null) DateTimeUtils.getReadableDate(timestamp) else null,

        hasCoupon = false
    )
}

fun Award.asTownHallAward(): AwardDetailUI{
    return AwardDetailUI(
        imageUrl = imageUrl,
        title = name,
        description = description ?: "",
        valueLabel = if(value != null) "Euro" else null,
        value = value?.toString(),
        header = null,
        emissionDate = if(timestamp != null) DateTimeUtils.getReadableDate(timestamp) else null,

        hasCoupon = true,
        qrCode = coupon?.code,
        state = if(coupon?.redemptionDate != null) "RISCATTATO" else "DA RISCATTARE",
        stateColor = this.getRightColor(),
        expiringDate = if(coupon?.expireDate != null) DateTimeUtils.getReadableDate(coupon.expireDate) else null,

        refundLabel = if(coupon?.redemptionDate != null) "Data riscossione" else null,
        refundDate = if(coupon?.redemptionDate!= null) DateTimeUtils.getReadableDate(coupon.redemptionDate) else null,

        refundType = "Riscattabile in comune"


    )
}

fun Award.asShopAward(): AwardDetailUI{
    return AwardDetailUI(
        imageUrl = imageUrl,
        title = name,
        description = description ?: "",
        valueLabel = "Punti",
        value = value?.toString(),
        header = null,
        emissionDate = if(timestamp != null) DateTimeUtils.getReadableDate(timestamp) else null,

        hasCoupon = true,
        qrCode = coupon?.code,
        state = if(coupon?.redemptionDate != null) "RISCATTATO" else "DA RISCATTARE",
        stateColor = if(coupon?.redemptionDate != null) R.color.green else R.color.red_main,
        expiringDate = if(coupon?.expireDate != null) DateTimeUtils.getReadableDate(coupon.expireDate) else null,
        refundLabel = if(coupon?.redemptionDate != null) "Data riscossione" else null,
        refundDate = if(coupon?.redemptionDate != null) DateTimeUtils.getReadableDate(coupon.redemptionDate) else null,

        refundType = "Riscattabile nei negozi aderenti",
        articleName = coupon?.articleTitle,
        articleImage = coupon?.articleImage,
        shopName = coupon?.shopName,
        shopImage = coupon?.shopLogo

    )
}


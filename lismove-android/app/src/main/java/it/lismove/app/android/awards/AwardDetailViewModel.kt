package it.lismove.app.android.awards

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import it.lismove.app.android.R
import it.lismove.app.android.awards.data.Award
import it.lismove.app.android.awards.data.AwardAchievement
import it.lismove.app.android.awards.data.AwardRanking
import it.lismove.app.android.awards.parser.asAwardDetailUI

class AwardDetailViewModel: ViewModel() {
    fun getAchievementUIFromIntent(intent: Intent): AwardDetailUI{
        val type = intent.getStringExtra(AwardDetailActivity.INTENT_AWARD_TYPE)
        requireNotNull(type)
        if(type == AwardDetailActivity.INTENT_AWARD_TYPE_RANKING){
            val awardString = intent.getStringExtra(AwardDetailActivity.INTENT_RANKING_AWARD)
            val award = Gson().fromJson(awardString, AwardRanking::class.java)
            return AwardDetailUI(
                imageUrl = award.imageUrl,
                title = award.name,
                description = award.description ?: "",
                valueLabel = award.getTypeLabel(),
                value = award.value.toString()
            )
        }else if(type == AwardDetailActivity.INTENT_AWARD_TYPE_ACHIEVEMENT){
            val awardString = intent.getStringExtra(AwardDetailActivity.INTENT_ACHIEVEMENT_AWARD)
            val award = Gson().fromJson(awardString, AwardAchievement::class.java)
            return AwardDetailUI(
                imageUrl = award.imageUrl,
                title = award.name,
                description = award.description,
                valueLabel = award.getTypeLabel(),
                value = award.value.toString()
            )
        }else{
            val awardString = intent.getStringExtra(AwardDetailActivity.INTENT_USER_AWARD)
            val award = Gson().fromJson(awardString, Award::class.java)
           return award.asAwardDetailUI()
        }

    }

    fun getSimpleAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Primo premio classifica Ferrragosto",
            valueLabel = "Euro",
            value = "123",
            qrCode = null,
            state = null
        )
    }

    fun getPointAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Primo premio classifica Ferrragosto",
            valueLabel = "Punti",
            value = "123",
            hasCoupon = false,
            qrCode = null,
            state = null,
            emissionDate = "20 ottobre 2021",
        )
    }
    fun getPendingEuroAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Primo premio classifica Ferrragosto",
            valueLabel = "Euro",
            value = "123",
            emissionDate = "20 ottobre 2021",
            hasCoupon = true,
            qrCode = null,
            state = "DA RIMBORSARE",
            stateColor = R.color.red_main,
            refundDate = null,
            refundType = "Bonifico in conto corrente"
        )
    }

    fun getRefundedEuroAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Primo premio classifica Ferrragosto",
            valueLabel = "Euro",
            value = "123",
            emissionDate = "20 ottobre 2021",
            hasCoupon = true,
            qrCode = null,
            state = "RIMBORSATO",
            stateColor = R.color.green,
            refundDate = "20 ottobre 2021",
            refundLabel = "Data emissione bonifico",
            refundType = "Bonifico in conto corrente"
        )
    }



    fun getShopAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Buono di esempio del 20% da riscattare in negozio.",
            valueLabel = "Punti",
            hasCoupon = true,
            value = "123",
            qrCode = "BA12490",
            stateColor = R.color.red,
            state = "DA RISCATTARE",
            emissionDate = "20 ottobre 2021",
            expiringDate = "20 novembre 2021",
            refundType = "Riscattabile nei negozi aderenti",
            shopLabel = "Riscattabile presso",
            shopName = "Shop nextome",
        )
    }
    fun getShopRedeemedAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Buono di esempio del 20% da riscattare in negozio.",
            valueLabel = "Punti",
            hasCoupon = true,
            value = "123",
            qrCode = "BA12490",
            state = "RISCATTATO",
            stateColor = R.color.green,
            emissionDate = "20 ottobre 2021",
            expiringDate = "20 novembre 2021",
            refundType = "Riscattabile nei negozi aderenti",
            shopLabel = "Riscattato presso",
            shopName = "Shop nextome",
            articleName = "Articolo di demo",
            refundDate = "25 ottobre 2021",
            refundLabel = "Data riscossione"
        )
    }

    fun getShopGenericAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Buono di esempio del 20% da riscattare in negozio.",
            valueLabel = "Punti",
            hasCoupon = true,
            value = "123",
            qrCode = "BA12490",
            stateColor = R.color.red,
            state = "DA RISCATTARE",
            emissionDate = "20 ottobre 2021",
            expiringDate = "20 novembre 2021",
            refundType = "Riscattabile nei negozi aderenti",
        )
    }
    fun getShopGenericRedeemedAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Buono di esempio del 20% da riscattare in negozio.",
            valueLabel = "Punti",
            hasCoupon = true,
            value = "123",
            qrCode = "BA12490",
            state = "RISCATTATO",
            stateColor = R.color.green,
            emissionDate = "20 ottobre 2021",
            expiringDate = "20 novembre 2021",
            refundType = "Riscattabile nei negozi aderenti",
            shopLabel = "Riscattato presso",
            shopName = "Shop nextome",
            articleName = "Articolo di demo",
            refundDate = "25 ottobre 2021",
            refundLabel = "Data riscossione"
        )
    }

    fun getComuneRedeemedAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Buono di esempio del 20% da riscattare in negozio.",
            hasCoupon = true,
            qrCode = "BA12490",
            state = "RISCATTATO",
            stateColor = R.color.green,
            emissionDate = "20 ottobre 2021",
            expiringDate = "20 novembre 2021",
            refundType = "Riscattabile in comune",
            refundDate = "25 ottobre 2021",
            refundLabel = "Data riscossione"
        )
    }

    fun getComunePendingAwardDetailUI(): AwardDetailUI{
        return AwardDetailUI(
            imageUrl = "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
            title = "Primo premio",
            description = "Buono di 5 euro da ritirare in comune",
            hasCoupon = true,
            qrCode = "BA12490",
            state = "DA RISCATTARE",
            stateColor = R.color.red_main,
            emissionDate = "20 ottobre 2021",
            expiringDate = "20 novembre 2021",
            refundType = "Riscattabile in comune",

        )
    }

}

data class AwardDetailUI(
    val imageUrl: String?,
    val title: String,
    val description: String,
    val valueLabel: String? = null,
    val value: String? = null,
    val header: String? = null,
    val emissionDate: String? = null,

    val hasCoupon: Boolean = false,

    val qrCode: String? = null,
    val state: String? = null,
    val stateColor: Int? = null,
    val refundType: String? = null,
    val refundLabel: String? = null,
    val refundDate: String? = null,
    val expiringDate: String? = null,
    val shopLabel: String? = null,
    val shopName: String? = null,
    val showShopImage: Boolean = true,
    val shopImage: String? = null,
    val articleName: String? = null,
    val articleImage: String? = null

)
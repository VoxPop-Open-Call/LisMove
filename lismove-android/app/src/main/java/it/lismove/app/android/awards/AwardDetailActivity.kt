package it.lismove.app.android.awards

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import coil.load
import coil.transform.CircleCropTransformation
import com.google.gson.Gson
import com.google.zxing.EncodeHintType
import it.lismove.app.android.R
import it.lismove.app.android.awards.data.Award
import it.lismove.app.android.awards.data.AwardAchievement
import it.lismove.app.android.awards.data.AwardRanking
import it.lismove.app.android.databinding.ActivityAwardDetailBinding
import it.lismove.app.android.general.utils.PxDpConversionUtils
import net.glxn.qrgen.android.QRCode
import org.koin.android.ext.android.inject

class AwardDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityAwardDetailBinding
     val viewModel: AwardDetailViewModel by inject()
    companion object{
        const val INTENT_AWARD_TYPE = "INTENT_AWARD_TYPE"
        const val INTENT_AWARD_TYPE_RANKING = "INTENT_AWARD_TYPE_RANKING"
        const val INTENT_AWARD_TYPE_USER_AWARD = "INTENT_AWARD_TYPE_USER_AWARD"
        const val INTENT_AWARD_TYPE_ACHIEVEMENT = "INTENT_AWARD_TYPE_ACHIEVEMENT"
        const val INTENT_RANKING_AWARD = "INTENT_RANKING_AWARD"
        const val INTENT_ACHIEVEMENT_AWARD = "INTENT_ACHIEVEMENT_AWARD"
        const val INTENT_USER_AWARD = "INTENT_ACHIEVEMENT_AWARD"


        fun getRankingAwardIntent(ctx: Context, awardRanking: AwardRanking): Intent{
            return Intent(ctx, AwardDetailActivity::class.java).apply {
                putExtra(INTENT_AWARD_TYPE, INTENT_AWARD_TYPE_RANKING)
                putExtra(INTENT_RANKING_AWARD, Gson().toJson(awardRanking))
            }
        }

        fun getAchievementAwardIntent(ctx: Context, achievementRanking: AwardAchievement): Intent{
            return Intent(ctx, AwardDetailActivity::class.java).apply {
                putExtra(INTENT_AWARD_TYPE, INTENT_AWARD_TYPE_ACHIEVEMENT)
                putExtra(INTENT_ACHIEVEMENT_AWARD, Gson().toJson(achievementRanking))
            }
        }

        fun getUserAwardIntent(ctx: Context, userAchievement: Award): Intent{
            return Intent(ctx, AwardDetailActivity::class.java).apply {
                putExtra(INTENT_AWARD_TYPE, INTENT_AWARD_TYPE_USER_AWARD)
                putExtra(INTENT_USER_AWARD, Gson().toJson(userAchievement))
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val detail = viewModel.getAchievementUIFromIntent(intent)
        updateUI(detail)
    }

    fun updateUI(detail: AwardDetailUI){
        with(binding){
            awardTitle.text = detail.title
            awardHeader.text = detail.header ?: ""

            awardDescription.text = detail.description

            awardValue.isVisible = detail.value.isNullOrEmpty().not()
            awardValue.text = detail.value

            awardValueLabel.text = detail.valueLabel
            awardValueLabel.isVisible = detail.valueLabel.isNullOrEmpty().not()


            awardEmissionDate.text = detail.emissionDate
            awardEmissionDate.isVisible = detail.emissionDate.isNullOrEmpty().not()
            awardEmissionDateLabel.isVisible = detail.emissionDate.isNullOrEmpty().not()
            bindCoupon(detail)




            if(detail.imageUrl != null){
                awardDetailImage.load(detail.imageUrl)
            }
        }
    }

    private fun bindCoupon(detail: AwardDetailUI) {
        with(binding){
            //couponGroup.isVisible = true
            couponLayout.isVisible = detail.hasCoupon

            qrCodeText.isVisible = detail.qrCode.isNullOrEmpty().not()
            qrCodeText.text = detail.qrCode
            qrCodeImage.isVisible = detail.qrCode.isNullOrEmpty().not()



            detail.qrCode?.let {

                val qrCodeBitmap = QRCode.from(it).withHint(EncodeHintType.MARGIN, "0").withSize(
                    PxDpConversionUtils.convertDpToPx(this@AwardDetailActivity, 200f).toInt(),
                    PxDpConversionUtils.convertDpToPx(this@AwardDetailActivity, 200f).toInt()).bitmap()
                qrCodeImage.setImageBitmap(qrCodeBitmap)
                qrCodeImage.setBackgroundColor(getColor(R.color.red))

            }

            bindCouponState(detail)
            bindCouponRefundType(detail)
            bindCouponRefundDate(detail)
            bindCouponExpireDate(detail)
            bindCouponShop(detail)
            bindCouponArticle(detail)
        }

    }

    private fun bindCouponArticle(detail: AwardDetailUI) {
        with(binding){
            if(detail.articleName.isNullOrEmpty().not()){
                couponArticle.isVisible = true
                couponArticleImage.isVisible = true
                couponArticleLabel.isVisible = true
                couponArticle.text = detail.articleName
                if(detail.articleImage.isNullOrEmpty()){
                    val avatarPic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab)

                    couponArticleImage.load(avatarPic)
                }else{
                    couponArticleImage.load(detail.articleImage){
                        transformations(CircleCropTransformation())
                    }
                }

            }else{
                couponArticle.isVisible = false
                couponArticleImage.isVisible = false
                couponArticleLabel.isVisible = false
            }
        }
    }

    private fun bindCouponShop(detail: AwardDetailUI) {
            with(binding){
                if(detail.shopName.isNullOrEmpty().not() && detail.hasCoupon){
                    couponRedeemedAt.isVisible = true
                    couponRedeemedAtImage.isVisible = true
                    couponeRedeemedAtLabel.isVisible = true
                    couponeRedeemedAtLabel.text = detail.shopLabel
                    couponRedeemedAt.text = detail.shopName
                    couponRedeemedAtImage.visibility = if(detail.showShopImage) View.VISIBLE else View.INVISIBLE

                    if(detail.shopImage.isNullOrEmpty()){
                        val avatarPic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab)

                        couponRedeemedAtImage.load(avatarPic)
                    }else{
                        couponRedeemedAtImage.load(detail.shopImage){
                            transformations(CircleCropTransformation())
                        }
                    }
                }else{
                    couponRedeemedAt.isVisible = false
                    couponRedeemedAtImage.isVisible = false
                    couponeRedeemedAtLabel.isVisible = false
                }
            }
    }

    private fun bindCouponExpireDate(detail: AwardDetailUI) {
        with(binding){

            if(detail.expiringDate.isNullOrEmpty().not() && detail.hasCoupon){
                couponExpireDate.text = detail.expiringDate
                couponExpireDate.isVisible = true
                couponExpireDateLabel.isVisible = true
            }else{
                couponExpireDate.isVisible = false
                couponExpireDateLabel.isVisible = false
            }
        }
    }

    private fun bindCouponRefundDate(detail: AwardDetailUI) {

        with(binding){

            if(detail.refundDate.isNullOrEmpty().not() && detail.hasCoupon){
                couponRefundDate.text = detail.refundDate
                couponRefundDateLabel.text = detail.refundLabel
                couponRefundDate.isVisible = true
                couponRefundDateLabel.isVisible = true
            }else{
                couponRefundDate.isVisible = false
                couponRefundDateLabel.isVisible = false
            }
        }
    }

    private fun bindCouponRefundType(detail: AwardDetailUI) {
        with(binding){
            if(detail.hasCoupon && detail.refundType.isNullOrEmpty().not()){
                couponRefundType.text = detail.refundType
                couponRefundType.isVisible = true
                couponRefundTypeLabel.isVisible = true

            }else{
                couponRefundType.isVisible = false
                couponRefundTypeLabel.isVisible = false
            }
        }
    }

    private fun bindCouponState(detail: AwardDetailUI) {
        with(binding){
            if(detail.hasCoupon && detail.state.isNullOrEmpty().not()){
                couponStateLabel.isVisible = true
                couponState.isVisible = true
                couponState.text = detail.state
                couponState.setTextColor(getColor(detail.stateColor ?: R.color.red_main))
            }else{
                couponStateLabel.isVisible = false
                couponState.isVisible = false
            }
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
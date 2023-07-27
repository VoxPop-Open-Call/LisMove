package it.lismove.app.android.gaming.ui.parser

import it.lismove.app.android.gaming.apiService.data.Achievement
import it.lismove.app.android.gaming.data.AchievementWithOrganization
import it.lismove.app.android.gaming.ui.data.AchievementItemUI
import it.lismove.app.common.DateTimeUtils
import kotlin.math.roundToInt

fun AchievementWithOrganization.asAchievementItemUI(): AchievementItemUI{
    val organizationLabel = if(organization!= null) "${organization.title}" else null

    val percentage = if(achievement.fullfilled) 100 else achievement.score * 100 / achievement.target
    val limitedTarget = if(achievement.fullfilled) achievement.target else achievement.score
    val countdown = if(achievement.endDate != null) "${DateTimeUtils.daysUntil(achievement.endDate)} GIORNI" else null
    val limitedTargetFormatted = "%.2f".format(limitedTarget)
    val percentageValue = "${limitedTargetFormatted}/${achievement.target.roundToInt()} ${achievement.getLabelForType()}"
    return AchievementItemUI(
        achievement.id.toString(),
        achievement.name,
        organizationLabel,
        percentage.toInt(),
        percentageValue,
        achievement.logo,
        achievement.fullfilled,
        countdown
    )
}

fun Achievement.asAchievementItemUI(): AchievementItemUI{
    val organizationLabel = organizationTitle

    val percentage = if(fullfilled) 100 else score * 100 / target
    val limitedTarget = if(fullfilled) target else score
    val countdown = if(endDate != null) "${DateTimeUtils.daysUntil(endDate)} GIORNI" else null
    val limitedTargetFormatted = "%.2f".format(limitedTarget)
    val percentageValue = "${limitedTargetFormatted}/${target.roundToInt()} ${getLabelForType()}"
    return AchievementItemUI(
        id.toString(),
        name,
        organizationLabel,
        percentage.toInt(),
        percentageValue,
        logo,
        fullfilled,
        countdown
    )
}

package net.nextome.lismove_sdk.sessionPoints.parser

import net.nextome.lismove_sdk.sessionPoints.data.PointsSummary
import net.nextome.lismove_sdk.sessionPoints.data.SessionPoints

fun SessionPoints.asPointsSummary(): PointsSummary {
    var initiativePoints = 0
    initiativePointOrganizations.forEach {  initiativePoints +=it.points  }
    return PointsSummary(
        nationalPoints,
        initiativePoints
    )
}
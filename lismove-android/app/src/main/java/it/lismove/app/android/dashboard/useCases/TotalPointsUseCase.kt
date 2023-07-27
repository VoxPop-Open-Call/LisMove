package it.lismove.app.android.dashboard.useCases

import it.lismove.app.android.dashboard.data.RankingPointData
import it.lismove.app.room.entity.LisMoveUser

interface TotalPointsUseCase {
    suspend fun getTotalNationalPoints(user: LisMoveUser): RankingPointData
    suspend fun getTotalInitiativePoints(user: LisMoveUser): List<RankingPointData>
    suspend fun getTotalActiveInitiativePoints(user: LisMoveUser): List<RankingPointData>
}
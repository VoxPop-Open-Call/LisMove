package it.lismove.app.android.dashboard.useCases

import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.dashboard.data.RankingPointData
import it.lismove.app.android.gaming.repository.RankingRepository
import it.lismove.app.room.entity.LisMoveUser
import timber.log.Timber

class TotalPointsUseCaseImpl(
    private val rankingRepository: RankingRepository,
    private val userRepository: UserRepository): TotalPointsUseCase {

    override suspend fun getTotalNationalPoints(user: LisMoveUser): RankingPointData {
        Timber.d("getTotalPoints for ${user.username}")
        val globalPoints = rankingRepository.getMainRanking().rankingPositions?.first { it.username.equals(user.username) }?.points ?: 0
        return RankingPointData(null, globalPoints.toInt(), null,"Community")
    }

    override suspend fun getTotalActiveInitiativePoints(user: LisMoveUser): List<RankingPointData> {
        val enrollmentWithOrganization = userRepository.getActiveInitiatives(user.uid)
        val points = arrayListOf<RankingPointData>()
        enrollmentWithOrganization.forEach {
            points.add(RankingPointData(it.organization.notificationLogo, it.enrollment.points ?: 0,  it.organization.id,"Progetto ${it.organization.title}"))
        }
        return points
    }

    override suspend fun getTotalInitiativePoints(user: LisMoveUser): List<RankingPointData> {
        val enrollmentWithOrganization = userRepository.getInitiatives(user.uid)
        val points = arrayListOf<RankingPointData>()
        enrollmentWithOrganization.forEach {
            points.add(RankingPointData(
                it.organization.notificationLogo,
                it.enrollment.points ?: 0,
                it.organization.id,
                "Progetto ${it.organization.title}"
            ))
        }

        return points
    }
}
package it.lismove.app.room.entity.QueryData

import androidx.room.Embedded
import androidx.room.Relation
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.OrganizationEntity
import it.lismove.app.room.entity.OrganizationSessionPointEntity

data class OrganizationWithSessionPoint (

    @Embedded
    val pointEntity: OrganizationSessionPointEntity,
    @Relation(
        parentColumn = "organizationId",
        entityColumn = "id"
    )
    val organization: OrganizationEntity

)
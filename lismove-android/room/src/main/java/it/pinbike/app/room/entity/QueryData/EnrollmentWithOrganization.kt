package it.lismove.app.room.entity.QueryData

import androidx.room.Embedded
import androidx.room.Relation
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.OrganizationEntity

data class EnrollmentWithOrganization(

    @Embedded val enrollment: EnrollmentEntity,
    @Relation(
        parentColumn = "organization",
        entityColumn = "id"
    )
    val organization: OrganizationEntity
)
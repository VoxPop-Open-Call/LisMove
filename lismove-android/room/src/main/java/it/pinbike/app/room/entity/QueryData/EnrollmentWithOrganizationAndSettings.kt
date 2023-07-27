package it.lismove.app.room.entity.QueryData

import androidx.room.Embedded
import androidx.room.Relation
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.OrganizationEntity
import it.lismove.app.room.entity.SettingsEntity
import kotlin.reflect.KMutableProperty

data class EnrollmentWithOrganizationAndSettings (
    @Embedded
    val enrollment: EnrollmentEntity,
    @Relation(
        parentColumn = "organization",
        entityColumn = "id"
    )
    val organization: OrganizationEntity,
    @Relation(
        parentColumn = "organization",
        entityColumn = "organizationId"
    )
    val settings: SettingsEntity
)
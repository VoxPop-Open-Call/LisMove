package it.lismove.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class EnrollmentEntity (
    val activationDate: Long?,
    val code: String,
    @PrimaryKey
    val id: Long,
    val organization: Long,
    val startDate: Long,
    val endDate: Long,
    val user: String?,
    val points: Int?
){

}
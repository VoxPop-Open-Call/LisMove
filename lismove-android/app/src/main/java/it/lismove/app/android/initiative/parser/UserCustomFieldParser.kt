package it.lismove.app.android.initiative.parser

import it.lismove.app.android.initiative.apiService.data.CustomFieldValueDao
import it.lismove.app.android.initiative.data.UserCustomField

fun UserCustomField.asCustomFieldValueDao(): CustomFieldValueDao{
    return CustomFieldValueDao(
        enrollment = eid,
        customField = customFieldId,
        value = value
    )
}


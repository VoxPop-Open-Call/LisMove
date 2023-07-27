package it.lismove.app.room.entity

import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class SettingsEntityTest {


    @Test
    fun `get active multiplier in between dates`() {
        val settingsEntity = SettingsEntity(
            organizationId = 1,
            startDateBonus = "2021-10-20",
            endDateBonus = "2021-10-30",
            multiplier = 2)
        val date = getDateFromString("2021-10-25", null)
        val actualMultiplier = settingsEntity.getActiveMultiplier(date)
        assertEquals(2, actualMultiplier)
    }

    @Test
    fun `get active multiplier on start date`() {
        val settingsEntity = SettingsEntity(
            organizationId = 1,
            startDateBonus = "2021-10-20",
            endDateBonus = "2021-10-30",
            multiplier = 2)
        val date = getDateFromString("2021-10-20", null)
        val actualMultiplier = settingsEntity.getActiveMultiplier(date)
        assertEquals(2, actualMultiplier)
    }

    @Test
    fun `get active multiplier on end date`() {
        val settingsEntity = SettingsEntity(
            organizationId = 1,
            startDateBonus = "2021-10-20",
            endDateBonus = "2021-10-30",
            multiplier = 2)
        val date = getDateFromString("2021-10-30", null)
        val actualMultiplier = settingsEntity.getActiveMultiplier(date)
        assertEquals(2, actualMultiplier)
    }

    @Test
    fun `discard multiplier if not in dates`() {
        val settingsEntity = SettingsEntity(
            organizationId = 1,
            startDateBonus = "2021-10-20",
            endDateBonus = "2021-10-30",
            multiplier = 2)
        val date = getDateFromString("2021-10-15", null)
        val actualMultiplier = settingsEntity.getActiveMultiplier(date)
        assertEquals(1, actualMultiplier)
    }

    @Test
    fun `get active multiplier in datetime`() {
        val settingsEntity = SettingsEntity(
            organizationId = 1,
            startDateBonus = "2021-10-20",
            endDateBonus = "2021-10-30",
            multiplier = 2,
            startTimeBonus = "08:00",
            endTimeBonus = "10:00")
        val date = getDateFromString("2021-10-25", "08:30")
        val actualMultiplier = settingsEntity.getActiveMultiplier(date)
        assertEquals(2, actualMultiplier)
    }

    @Test
    fun `get active multiplier not in datetime`() {
        val settingsEntity = SettingsEntity(
            organizationId = 1,
            startDateBonus = "2021-10-20",
            endDateBonus = "2021-10-30",
            multiplier = 2,
            startTimeBonus = "08:00",
            endTimeBonus = "10:00",
            isActiveTimeSlotBonus = true)
        val date = getDateFromString("2021-10-20", "16:30")
        val actualMultiplier = settingsEntity.getActiveMultiplier(date)
        assertEquals(1, actualMultiplier)
    }

    @Test
    fun `get active multiplier on start time`() {
        val settingsEntity = SettingsEntity(
            organizationId = 1,
            startDateBonus = "2021-10-20",
            endDateBonus = "2021-10-30",
            multiplier = 2,
            startTimeBonus = "08:00",
            endTimeBonus = "10:00",
            isActiveTimeSlotBonus = true)
        val date = getDateFromString("2021-10-20", "08:00")
        val actualMultiplier = settingsEntity.getActiveMultiplier(date)
        assertEquals(2, actualMultiplier)
    }

    @Test
    fun `get active multiplier on end time`() {
        val settingsEntity = SettingsEntity(
            organizationId = 1,
            startDateBonus = "2021-10-20",
            endDateBonus = "2021-10-30",
            multiplier = 2,
            startTimeBonus = "08:00",
            endTimeBonus = "10:00",
            isActiveTimeSlotBonus = true)
        val date = getDateFromString("2021-10-20", "10:00")
        val actualMultiplier = settingsEntity.getActiveMultiplier(date)
        assertEquals(2, actualMultiplier)
    }

    private fun getDateFromString(date: String, time: String?): Date{
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val timeOrMidnight = time ?: "00:00:00"
        return formatter.parse("${date}T$timeOrMidnight")!!
    }

}
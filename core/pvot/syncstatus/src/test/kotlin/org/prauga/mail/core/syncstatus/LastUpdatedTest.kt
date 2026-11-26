// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.core.syncstatus

import assertk.assertThat
import assertk.assertions.isEqualTo
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test

class LastUpdatedTest {
    private val zone = ZoneId.of("UTC")
    private val now = ZonedDateTime.of(2026, 11, 26, 15, 0, 0, 0, zone)
    private val clock = Clock.fixed(now.toInstant(), zone)

    @Test
    fun `never synced`() {
        assertThat(lastUpdatedOf(0L, clock)).isEqualTo(LastUpdated.Never)
    }

    @Test
    fun `under a minute is just now`() {
        assertThat(lastUpdatedOf(now.minusSeconds(30).millis(), clock)).isEqualTo(LastUpdated.JustNow)
    }

    @Test
    fun `under an hour counts minutes`() {
        assertThat(lastUpdatedOf(now.minusMinutes(5).millis(), clock)).isEqualTo(LastUpdated.MinutesAgo(5))
    }

    @Test
    fun `earlier today shows the time`() {
        assertThat(lastUpdatedOf(now.withHour(9).millis(), clock)).isEqualTo(LastUpdated.Today(LocalTime.of(9, 0)))
    }

    @Test
    fun `before today shows the date`() {
        assertThat(lastUpdatedOf(now.minusDays(2).millis(), clock))
            .isEqualTo(LastUpdated.Earlier(LocalDate.of(2026, 11, 24)))
    }

    private fun ZonedDateTime.millis() = toInstant().toEpochMilli()
}

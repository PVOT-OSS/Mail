// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import assertk.assertThat
import assertk.assertions.isEqualTo
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.Test

class MessageDateFormatterTest {
    private val zone = ZoneId.of("UTC")
    private val now = ZonedDateTime.of(2026, 11, 26, 15, 0, 0, 0, zone)
    private val testSubject = MessageDateFormatter(
        clock = Clock.fixed(now.toInstant(), zone),
        locale = Locale.US,
        yesterday = "Yesterday",
    )

    @Test
    fun `today shows the time`() {
        assertThat(testSubject.format(now.withHour(9).withMinute(5).millis())).isEqualTo("9:05 AM")
    }

    @Test
    fun `the day before shows yesterday`() {
        assertThat(testSubject.format(now.minusDays(1).withHour(23).millis())).isEqualTo("Yesterday")
    }

    @Test
    fun `within the last week shows the weekday`() {
        assertThat(testSubject.format(now.minusDays(3).millis())).isEqualTo("Monday")
    }

    @Test
    fun `older mail shows a short date`() {
        assertThat(testSubject.format(now.minusDays(30).millis())).isEqualTo("10/27/26")
    }

    private fun ZonedDateTime.millis() = toInstant().toEpochMilli()
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val DAYS_SHOWN_AS_WEEKDAY = 6

class MessageDateFormatter(
    private val clock: Clock,
    private val locale: Locale,
    private val yesterday: String,
) {
    private val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    private val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)

    fun format(epochMillis: Long): String {
        val zone: ZoneId = clock.zone
        val dateTime = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val today = LocalDate.now(clock)
        val days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), today)
        return when {
            days == 0L -> time.format(dateTime)
            days == 1L -> yesterday
            days in 2..DAYS_SHOWN_AS_WEEKDAY -> dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            else -> date.format(dateTime)
        }
    }
}

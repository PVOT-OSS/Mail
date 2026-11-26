// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_SHOWN_AS_AGO = 59L

sealed interface LastUpdated {
    data object Never : LastUpdated
    data object JustNow : LastUpdated
    data class MinutesAgo(val minutes: Long) : LastUpdated
    data class Today(val time: LocalTime) : LastUpdated
    data class Earlier(val date: LocalDate) : LastUpdated
}

fun lastUpdatedOf(epochMillis: Long, clock: Clock): LastUpdated {
    if (epochMillis <= 0L) return LastUpdated.Never
    val minutes = (clock.millis() - epochMillis).coerceAtLeast(0L) / MILLIS_PER_MINUTE
    val then = Instant.ofEpochMilli(epochMillis).atZone(clock.zone)
    return when {
        minutes < 1 -> LastUpdated.JustNow
        minutes <= MINUTES_SHOWN_AS_AGO -> LastUpdated.MinutesAgo(minutes)
        then.toLocalDate() == LocalDate.now(clock) -> LastUpdated.Today(then.toLocalTime())
        else -> LastUpdated.Earlier(then.toLocalDate())
    }
}

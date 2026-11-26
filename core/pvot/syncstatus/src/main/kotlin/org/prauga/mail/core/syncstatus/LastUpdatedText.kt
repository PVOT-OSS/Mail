// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.core.syncstatus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import java.time.Clock
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay

private const val TICK_MILLIS = 30_000L

@Composable
fun lastUpdatedText(epochMillis: Long): String? {
    var tick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(epochMillis) {
        while (true) {
            delay(TICK_MILLIS)
            tick++
        }
    }
    val locale = LocalConfiguration.current.locales[0]
    val updated = remember(epochMillis, tick) { lastUpdatedOf(epochMillis, Clock.systemDefaultZone()) }
    return when (updated) {
        LastUpdated.Never -> null

        LastUpdated.JustNow -> stringResource(R.string.syncstatus_updated_just_now)

        is LastUpdated.MinutesAgo -> pluralStringResource(
            R.plurals.syncstatus_updated_minutes_ago,
            updated.minutes.toInt(),
            updated.minutes.toInt(),
        )

        is LastUpdated.Today -> stringResource(
            R.string.syncstatus_updated_at,
            updated.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)),
        )

        is LastUpdated.Earlier -> stringResource(
            R.string.syncstatus_updated_on,
            updated.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)),
        )
    }
}

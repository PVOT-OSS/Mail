// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature

import net.thunderbird.feature.funding.api.FundingSettings

@Suppress("TooManyFunctions", "EmptyFunctionBlock")
internal class PvotMailFundingSettings : FundingSettings {

    override fun isReady(): Boolean = true

    override fun getReminderReferenceTimestamp(): Long = 0L

    override suspend fun setReminderReferenceTimestamp(timestamp: Long) {}

    override fun getReminderShownTimestamp() = 0L

    override suspend fun setReminderShownTimestamp(timestamp: Long) {}

    override fun getReminderShownCount(): Int = 100

    override suspend fun setReminderShownCount(count: Int) {}

    override suspend fun incrementReminderShownCount() {}
}

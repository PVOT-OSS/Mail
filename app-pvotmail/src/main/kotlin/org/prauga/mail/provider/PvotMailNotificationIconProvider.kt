// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.provider

import app.k9mail.core.android.common.provider.NotificationIconResourceProvider
import org.prauga.mail.R

internal class PvotMailNotificationIconProvider : NotificationIconResourceProvider {
    override val pushNotificationIcon: Int = R.drawable.ic_notification_pvot_mail
}

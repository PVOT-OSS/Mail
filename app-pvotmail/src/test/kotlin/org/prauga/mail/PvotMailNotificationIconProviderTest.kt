// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import org.prauga.mail.provider.PvotMailNotificationIconProvider

class PvotMailNotificationIconProviderTest {
    @Test
    fun `provides the Pvot Mail notification icon`() {
        val testSubject = PvotMailNotificationIconProvider()

        val icon = testSubject.pushNotificationIcon

        assertThat(icon).isEqualTo(R.drawable.ic_notification_pvot_mail)
    }
}

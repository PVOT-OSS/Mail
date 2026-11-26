// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import app.k9mail.legacy.message.controller.MessageReference
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import kotlin.test.Test
import net.thunderbird.feature.account.AccountIdFactory

class ThreadEntryTest {
    private val account = AccountIdFactory.create()

    @Test
    fun `the opened message and the newest one start expanded`() {
        val testSubject = listOf(entry(1), entry(2), entry(3), entry(4))

        assertThat(testSubject.initiallyExpanded(reference(2))).containsExactlyInAnyOrder(2L, 4L)
    }

    @Test
    fun `opening the newest message expands only it`() {
        val testSubject = listOf(entry(1), entry(2))

        assertThat(testSubject.initiallyExpanded(reference(2))).containsExactlyInAnyOrder(2L)
    }

    private fun reference(key: Long) = MessageReference(account, 1, key.toString())

    private fun entry(key: Long) = ThreadEntry(
        key = key,
        reference = reference(key),
        senderName = "",
        senderAddress = "",
        sentAt = key,
        preview = "",
        isUnread = false,
    )
}

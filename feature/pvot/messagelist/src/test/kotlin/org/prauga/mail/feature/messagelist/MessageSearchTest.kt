// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import app.k9mail.legacy.message.controller.MessageReference
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import net.thunderbird.feature.account.AccountIdFactory

class MessageSearchTest {
    private val testSubject = listOf(
        row(1, sender = "Alice", subject = "Lunch", preview = "Pizza at noon"),
        row(2, sender = "Bob", subject = "Invoice 42", preview = "Attached"),
        row(3, sender = "Carol", subject = "Trip", preview = "Lunch in Rome"),
    )

    @Test
    fun `a blank query keeps every row`() {
        assertThat(testSubject.matching("  ").map { it.key }).containsExactly(1L, 2L, 3L)
    }

    @Test
    fun `the query matches sender, subject and preview, ignoring case`() {
        assertThat(testSubject.matching("lunch").map { it.key }).containsExactly(1L, 3L)
        assertThat(testSubject.matching("BOB").map { it.key }).containsExactly(2L)
    }

    private fun row(key: Long, sender: String, subject: String, preview: String) = MessageRow(
        key = key,
        sender = sender,
        senderKey = sender,
        subject = subject,
        preview = preview,
        date = "",
        isUnread = false,
        isStarred = false,
        hasAttachments = false,
        threadCount = 0,
        reference = MessageReference(AccountIdFactory.create(), 1, key.toString()),
    )
}

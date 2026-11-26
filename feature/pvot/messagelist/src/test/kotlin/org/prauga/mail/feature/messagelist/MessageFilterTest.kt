// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import app.k9mail.legacy.message.controller.MessageReference
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import net.thunderbird.feature.account.AccountIdFactory

class MessageFilterTest {
    private val testSubject = listOf(
        row(1, unread = true),
        row(2, starred = true),
        row(3, attachments = true),
        row(4),
    )

    @Test
    fun `a disabled filter keeps every row`() {
        assertThat(testSubject.filteredBy(FilterState(enabled = false)).keys()).containsExactly(1L, 2L, 3L, 4L)
    }

    @Test
    fun `the default filter keeps unread rows`() {
        assertThat(testSubject.filteredBy(FilterState(enabled = true)).keys()).containsExactly(1L)
    }

    @Test
    fun `several filters keep rows matching any of them`() {
        val state = FilterState(enabled = true, include = setOf(MessageFilter.UNREAD, MessageFilter.FLAGGED))

        assertThat(testSubject.filteredBy(state).keys()).containsExactly(1L, 2L)
    }

    @Test
    fun `only with attachments narrows the included rows`() {
        val state = FilterState(enabled = true, include = emptySet(), onlyWithAttachments = true)

        assertThat(testSubject.filteredBy(state).keys()).containsExactly(3L)
    }

    @Test
    fun `only with attachments combines with an include filter`() {
        val rows = testSubject + row(5, unread = true, attachments = true)
        val state = FilterState(enabled = true, onlyWithAttachments = true)

        assertThat(rows.filteredBy(state).keys()).containsExactly(5L)
    }

    private fun List<MessageRow>.keys() = map { it.key }

    private fun row(key: Long, unread: Boolean = false, starred: Boolean = false, attachments: Boolean = false) =
        MessageRow(
            key = key,
            sender = "",
            senderKey = "",
            subject = "",
            preview = "",
            date = "",
            isUnread = unread,
            isStarred = starred,
            hasAttachments = attachments,
            threadCount = 0,
            reference = MessageReference(AccountIdFactory.create(), 1, key.toString()),
        )
}

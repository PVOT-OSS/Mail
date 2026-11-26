// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import app.k9mail.legacy.message.controller.MessageReference
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import net.thunderbird.feature.account.AccountIdFactory

class PendingArchivesTest {
    private val testSubject = PendingArchives()

    @Test
    fun `a pending archive hides its row`() {
        testSubject.add(row(1))

        assertThat(listOf(row(1), row(2)).without(testSubject.keys.value).map { it.key }).containsExactly(2L)
    }

    @Test
    fun `undo brings the row back and nothing is left to commit`() {
        testSubject.add(row(1))

        assertThat(testSubject.undo(1)).isTrue()
        assertThat(testSubject.take(1)).isNull()
        assertThat(testSubject.keys.value).isEmpty()
    }

    @Test
    fun `undo after the archive was committed does nothing`() {
        testSubject.add(row(1))
        testSubject.take(1)

        assertThat(testSubject.undo(1)).isFalse()
    }

    @Test
    fun `take all returns every pending row once`() {
        testSubject.add(row(1))
        testSubject.add(row(2))

        assertThat(testSubject.takeAll().map { it.key }).containsExactly(1L, 2L)
        assertThat(testSubject.takeAll()).isEmpty()
    }

    @Test
    fun `take returns the pending row`() {
        testSubject.add(row(3))

        assertThat(testSubject.take(3)?.key).isEqualTo(3L)
    }

    private fun row(key: Long) = MessageRow(
        key = key,
        sender = "",
        subject = "",
        preview = "",
        date = "",
        isUnread = false,
        isStarred = false,
        hasAttachments = false,
        threadCount = 0,
        reference = MessageReference(AccountIdFactory.create(), 1, key.toString()),
    )
}

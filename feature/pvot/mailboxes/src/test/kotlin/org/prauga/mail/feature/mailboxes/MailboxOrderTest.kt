// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.mailboxes

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import net.thunderbird.feature.mail.folder.FolderType

class MailboxOrderTest {
    @Test
    fun `special mailboxes come first in a fixed order, then folders by name`() {
        val testSubject = listOf(
            mailbox("Trash", FolderType.TRASH),
            mailbox("work", FolderType.REGULAR),
            mailbox("Sent", FolderType.SENT),
            mailbox("Archive", FolderType.ARCHIVE),
            mailbox("Inbox", FolderType.INBOX),
            mailbox("Bills", FolderType.REGULAR),
            mailbox("Drafts", FolderType.DRAFTS),
        )

        val ordered = testSubject.inMailboxOrder().map { it.name }

        assertThat(ordered).containsExactly("Inbox", "Drafts", "Sent", "Archive", "Trash", "Bills", "work")
    }

    private fun mailbox(name: String, type: FolderType) =
        Mailbox(folderId = name.hashCode().toLong(), name = name, type = type, unread = 0)

    @Test
    fun `nested folders show their last path segment and their depth`() {
        val testSubject = Mailbox(
            folderId = 1,
            name = "Work/Clients/Acme",
            type = FolderType.REGULAR,
            unread = 0,
            pathDelimiter = "/",
        )

        assertThat(testSubject.displayName).isEqualTo("Acme")
        assertThat(testSubject.depth).isEqualTo(2)
    }
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.mailboxes

import net.thunderbird.feature.mail.folder.FolderType

private val specialOrder = listOf(
    FolderType.INBOX,
    FolderType.DRAFTS,
    FolderType.SENT,
    FolderType.OUTBOX,
    FolderType.ARCHIVE,
    FolderType.SPAM,
    FolderType.TRASH,
)

internal fun List<Mailbox>.inMailboxOrder(): List<Mailbox> =
    sortedWith(
        compareBy<Mailbox> { mailbox ->
            specialOrder.indexOf(mailbox.type).takeIf { it >= 0 } ?: specialOrder.size
        }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name },
    )

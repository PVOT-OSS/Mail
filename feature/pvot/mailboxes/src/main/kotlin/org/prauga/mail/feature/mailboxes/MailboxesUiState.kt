// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.mailboxes

import net.thunderbird.feature.account.AccountId
import net.thunderbird.feature.mail.folder.FolderType

data class MailboxesUiState(
    val unifiedInboxUnread: Int = 0,
    val accounts: List<AccountMailboxes> = emptyList(),
)

data class AccountMailboxes(
    val accountId: AccountId,
    val title: String,
    val mailboxes: List<Mailbox>,
)

data class Mailbox(
    val folderId: Long,
    val name: String,
    val type: FolderType,
    val unread: Int,
)

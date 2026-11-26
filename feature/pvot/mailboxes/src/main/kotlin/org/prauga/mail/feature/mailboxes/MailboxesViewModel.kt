// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.mailboxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.k9mail.legacy.message.controller.MessageCountsProvider
import app.k9mail.legacy.ui.folder.DisplayFolder
import app.k9mail.legacy.ui.folder.DisplayFolderRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.thunderbird.core.android.account.LegacyAccount
import net.thunderbird.core.android.account.LegacyAccountManager
import net.thunderbird.feature.mail.folder.FolderRole
import net.thunderbird.feature.search.legacy.LocalMessageSearch

private const val STOP_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
class MailboxesViewModel(
    accountManager: LegacyAccountManager,
    displayFolderRepository: DisplayFolderRepository,
    messageCountsProvider: MessageCountsProvider,
    ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val accounts: Flow<List<AccountMailboxes>> = accountManager.observeAll()
        .map { all -> all.filter { it.isFinishedSetup } }
        .flatMapLatest { all ->
            if (all.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    all.map { account ->
                        displayFolderRepository.getDisplayFoldersFlow(account.id)
                            .map { folders -> account.toMailboxes(folders) }
                    },
                ) { it.toList() }
            }
        }

    private val unifiedInboxUnread: Flow<Int> = messageCountsProvider
        .getMessageCountsFlow(LocalMessageSearch.createUnifiedSearch(FolderRole.INBOX))
        .map { it.unread }

    val state: StateFlow<MailboxesUiState> = combine(unifiedInboxUnread, accounts) { unread, accounts ->
        MailboxesUiState(unifiedInboxUnread = unread, accounts = accounts)
    }
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MailboxesUiState())

    private fun LegacyAccount.toMailboxes(folders: List<DisplayFolder>) = AccountMailboxes(
        accountId = id,
        title = name?.takeIf { it.isNotBlank() } ?: email,
        mailboxes = folders.map { displayFolder ->
            Mailbox(
                folderId = displayFolder.folder.id,
                name = displayFolder.folder.name,
                type = displayFolder.folder.type,
                unread = displayFolder.unreadMessageCount,
            )
        }.inMailboxOrder(),
    )
}

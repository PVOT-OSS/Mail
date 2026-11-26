// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.mailboxes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.button.PvotFab
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import net.thunderbird.feature.account.AccountId
import net.thunderbird.feature.mail.folder.FolderType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MailboxesScreen(
    onOpenUnifiedInbox: () -> Unit,
    onOpenMailbox: (AccountId, Long) -> Unit,
    onNewMessage: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: MailboxesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MailboxesContent(
        state = state,
        onOpenUnifiedInbox = onOpenUnifiedInbox,
        onOpenMailbox = onOpenMailbox,
        onNewMessage = onNewMessage,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
internal fun MailboxesContent(
    state: MailboxesUiState,
    onOpenUnifiedInbox: () -> Unit,
    onOpenMailbox: (AccountId, Long) -> Unit,
    onNewMessage: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val listState = rememberLazyListState()
    val compact = rememberPvotTopBarCompact(listState)
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    PvotScreenScaffold(
        topBar = { MailboxesTopBar(compact = compact, onOpenSettings = onOpenSettings) },
        floatingAction = {
            PvotFab(
                icon = painterResource(R.drawable.mailboxes_ic_pencil_plus),
                onClick = onNewMessage,
                contentDescription = stringResource(R.string.mailboxes_new_message),
            )
        },
    ) { padding ->
        PvotScreen(
            state = listState,
            contentPadding = PvotScreenDefaults.contentPadding(
                top = padding.calculateTopPadding() + PvotSpacing.S,
                bottom = bottom + PvotSpacing.L,
            ),
            verticalArrangement = Arrangement.spacedBy(PvotSpacing.L),
            horizontalAlignment = Alignment.Start,
        ) {
            mailboxItems(state, onOpenUnifiedInbox, onOpenMailbox)
        }
    }
}

@Composable
private fun MailboxesTopBar(compact: Boolean, onOpenSettings: () -> Unit) {
    PvotTopBar(
        title = stringResource(R.string.mailboxes_title),
        compact = compact,
        actions = {
            PvotTopBarAction(
                icon = painterResource(R.drawable.mailboxes_ic_settings),
                contentDescription = stringResource(R.string.mailboxes_settings),
                onClick = onOpenSettings,
            )
        },
    )
}

private fun LazyListScope.mailboxItems(
    state: MailboxesUiState,
    onOpenUnifiedInbox: () -> Unit,
    onOpenMailbox: (AccountId, Long) -> Unit,
) {
    if (state.accounts.size > 1) {
        item(key = "unified") {
            PvotListGroup {
                item {
                    MailboxRow(
                        title = stringResource(R.string.mailboxes_all_inboxes),
                        icon = R.drawable.mailboxes_ic_stack_2,
                        unread = state.unifiedInboxUnread,
                        onClick = onOpenUnifiedInbox,
                    )
                }
            }
        }
    }
    items(state.accounts, key = { it.accountId.toString() }) { account ->
        PvotListGroup(title = account.title) {
            account.mailboxes.forEach { mailbox ->
                item {
                    MailboxRow(
                        title = mailbox.displayName,
                        depth = mailbox.depth,
                        icon = mailbox.type.icon(),
                        unread = mailbox.unread,
                        onClick = { onOpenMailbox(account.accountId, mailbox.folderId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MailboxRow(
    title: String,
    @DrawableRes icon: Int,
    unread: Int,
    onClick: () -> Unit,
    depth: Int = 0,
) {
    PvotListRow(
        title = title,
        leading = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = PvotSpacing.L * depth),
            )
        },
        trailing = if (unread > 0) {
            {
                Text(
                    text = unread.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            null
        },
        onClick = onClick,
    )
}

@DrawableRes
private fun FolderType.icon(): Int = when (this) {
    FolderType.INBOX -> R.drawable.mailboxes_ic_inbox
    FolderType.OUTBOX -> R.drawable.mailboxes_ic_mail_up
    FolderType.SENT -> R.drawable.mailboxes_ic_send
    FolderType.TRASH -> R.drawable.mailboxes_ic_trash
    FolderType.DRAFTS -> R.drawable.mailboxes_ic_file_pencil
    FolderType.ARCHIVE -> R.drawable.mailboxes_ic_archive
    FolderType.SPAM -> R.drawable.mailboxes_ic_alert_octagon
    FolderType.REGULAR -> R.drawable.mailboxes_ic_folder
}

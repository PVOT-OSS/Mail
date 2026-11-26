// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.mailboxes

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.designsystem.components.PvotGlassIconButton
import com.prauga.pvot.designsystem.components.PvotGlassIconButtonDefaults
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import net.thunderbird.feature.account.AccountId
import net.thunderbird.feature.mail.folder.FolderType
import org.koin.compose.viewmodel.koinViewModel
import com.prauga.pvot.designsystem.R as DesignSystemR

private const val QUARTER_TURN = 90f
private val AccountRowMinHeight = 56.dp

@Composable
fun MailboxesScreen(
    onOpenUnifiedInbox: (title: String) -> Unit,
    onOpenStarredMail: (title: String) -> Unit,
    onOpenMailbox: (accountId: AccountId, folderId: Long, title: String, subtitle: String) -> Unit,
    onNewMessage: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: MailboxesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MailboxesContent(
        state = state,
        onOpenUnifiedInbox = onOpenUnifiedInbox,
        onOpenStarredMail = onOpenStarredMail,
        onOpenMailbox = onOpenMailbox,
        onNewMessage = onNewMessage,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
internal fun MailboxesContent(
    state: MailboxesUiState,
    onOpenUnifiedInbox: (title: String) -> Unit,
    onOpenStarredMail: (title: String) -> Unit,
    onOpenMailbox: (accountId: AccountId, folderId: Long, title: String, subtitle: String) -> Unit,
    onNewMessage: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val listState = rememberLazyListState()
    var expanded by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val compact = rememberPvotTopBarCompact(listState)
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    PvotScreenScaffold(
        topBar = { MailboxesTopBar(compact = compact, onOpenSettings = onOpenSettings) },
        floatingAction = {
            PvotGlassIconButton(onClick = onNewMessage) {
                Icon(
                    painter = painterResource(R.drawable.mailboxes_ic_edit),
                    contentDescription = stringResource(R.string.mailboxes_new_message),
                )
            }
        },
    ) { padding ->
        PvotScreen(
            state = listState,
            contentPadding = PvotScreenDefaults.contentPadding(
                top = padding.calculateTopPadding() + PvotSpacing.S,
                bottom = bottom + PvotGlassIconButtonDefaults.Size + PvotSpacing.L * 2,
            ),
            verticalArrangement = Arrangement.spacedBy(PvotSpacing.L),
            horizontalAlignment = Alignment.Start,
        ) {
            mailboxItems(
                state = state,
                expanded = expanded,
                onToggleAccount = { key -> expanded = if (key in expanded) expanded - key else expanded + key },
                onOpenUnifiedInbox = onOpenUnifiedInbox,
                onOpenStarredMail = onOpenStarredMail,
                onOpenMailbox = onOpenMailbox,
            )
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
    expanded: List<String>,
    onToggleAccount: (String) -> Unit,
    onOpenUnifiedInbox: (title: String) -> Unit,
    onOpenStarredMail: (title: String) -> Unit,
    onOpenMailbox: (accountId: AccountId, folderId: Long, title: String, subtitle: String) -> Unit,
) {
    item(key = "favourites") {
        PvotListGroup {
            item {
                val title = stringResource(R.string.mailboxes_all_inboxes)
                MailboxRow(
                    title = title,
                    icon = R.drawable.mailboxes_ic_stack_2,
                    unread = state.unifiedInboxUnread,
                    onClick = { onOpenUnifiedInbox(title) },
                )
            }
            state.accounts.forEach { account ->
                val inbox = account.inbox ?: return@forEach
                item {
                    MailboxRow(
                        title = account.title,
                        icon = R.drawable.mailboxes_ic_inbox,
                        unread = inbox.unread,
                        onClick = {
                            onOpenMailbox(account.accountId, inbox.folderId, inbox.displayName, account.title)
                        },
                    )
                }
            }
            item {
                val title = stringResource(R.string.mailboxes_starred)
                MailboxRow(
                    title = title,
                    icon = R.drawable.mailboxes_ic_star,
                    unread = state.starredUnread,
                    onClick = { onOpenStarredMail(title) },
                )
            }
        }
    }
    accountItems(state, expanded, onToggleAccount, onOpenMailbox)
}

private fun LazyListScope.accountItems(
    state: MailboxesUiState,
    expanded: List<String>,
    onToggleAccount: (String) -> Unit,
    onOpenMailbox: (accountId: AccountId, folderId: Long, title: String, subtitle: String) -> Unit,
) {
    state.accounts.forEach { account ->
        val key = account.accountId.toString()
        val isExpanded = key in expanded
        item(key = "account-$key") {
            AccountRow(
                title = account.title,
                unread = account.inbox?.unread ?: 0,
                expanded = isExpanded,
                onClick = { onToggleAccount(key) },
            )
        }
        if (isExpanded && account.otherMailboxes.isNotEmpty()) {
            item(key = "folders-$key") {
                PvotListGroup {
                    account.otherMailboxes.forEach { mailbox ->
                        item {
                            MailboxRow(
                                title = mailbox.displayName,
                                depth = mailbox.depth,
                                icon = mailbox.type.icon(),
                                unread = mailbox.unread,
                                onClick = {
                                    onOpenMailbox(
                                        account.accountId,
                                        mailbox.folderId,
                                        mailbox.displayName,
                                        account.title,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountRow(title: String, unread: Int, expanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(if (expanded) QUARTER_TURN else 0f, label = "chevron")
    val stateLabel = stringResource(if (expanded) R.string.mailboxes_collapse else R.string.mailboxes_expand)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AccountRowMinHeight)
            .clickable(onClickLabel = stateLabel, onClick = onClick)
            .padding(horizontal = PvotSpacing.L),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        UnreadCount(unread)
        Icon(
            painter = painterResource(DesignSystemR.drawable.pvot_ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = PvotSpacing.S).rotate(rotation),
        )
    }
}

@Composable
private fun UnreadCount(unread: Int) {
    if (unread > 0) {
        Text(
            text = unread.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UnreadCount(unread)
                Icon(
                    painter = painterResource(DesignSystemR.drawable.pvot_ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = PvotSpacing.S),
                )
            }
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

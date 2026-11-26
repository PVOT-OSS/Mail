// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.activity.MessageHomeActivity
import com.fsck.k9.activity.compose.MessageActions
import com.prauga.pvot.designsystem.motion.PvotPageTransitions
import net.thunderbird.feature.account.AccountId
import net.thunderbird.feature.mail.folder.FolderRole
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.prauga.mail.feature.mailboxes.MailboxesScreen
import org.prauga.mail.feature.mailboxes.starredSearch
import org.prauga.mail.feature.messagelist.MessageListScreen

@Composable
fun PvotMailNavDisplay(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(MailboxesRoute)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = { PvotPageTransitions.push() },
        popTransitionSpec = { PvotPageTransitions.pop() },
        predictivePopTransitionSpec = { PvotPageTransitions.pop() },
        entryProvider = entryProvider {
            entry<MailboxesRoute> {
                MailboxesScreen(
                    onOpenUnifiedInbox = { title ->
                        backStack.add(
                            MessageListRoute.of(title, LocalMessageSearch.createUnifiedSearch(FolderRole.INBOX)),
                        )
                    },
                    onOpenStarredMail = { title -> backStack.add(MessageListRoute.of(title, starredSearch())) },
                    onOpenMailbox = { accountId, folderId, title ->
                        backStack.add(MessageListRoute.of(title, folderSearch(accountId, folderId)))
                    },
                    onNewMessage = { MessageActions.actionCompose(context) },
                    onOpenSettings = onOpenSettings,
                )
            }
            entry<MessageListRoute> { route ->
                MessageListScreen(
                    search = route.search,
                    title = route.title,
                    onOpenMessage = { reference -> openMessage(context, reference) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}

private fun folderSearch(accountId: AccountId, folderId: Long) = LocalMessageSearch().apply {
    addAccountId(accountId)
    addAllowedFolder(folderId)
}

private fun openMessage(context: Context, reference: MessageReference) {
    context.startActivity(
        MessageHomeActivity.actionDisplayMessageIntent(context, reference, messageViewOnly = true),
    )
}

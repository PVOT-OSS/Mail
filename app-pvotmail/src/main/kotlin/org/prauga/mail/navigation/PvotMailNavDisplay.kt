// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.activity.MessageHomeActivity
import com.fsck.k9.activity.compose.MessageActions
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.designsystem.adaptive.rememberPvotWindowLayout
import com.prauga.pvot.designsystem.motion.PvotPageTransitions
import net.thunderbird.feature.account.AccountId
import net.thunderbird.feature.mail.folder.FolderRole
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.prauga.mail.R
import org.prauga.mail.feature.mailboxes.MailboxesScreen
import org.prauga.mail.feature.mailboxes.starredSearch
import org.prauga.mail.feature.messagelist.MessageListScreen

@Composable
fun PvotMailNavDisplay(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(MailboxesRoute)
    val noMailboxSelected = stringResource(R.string.pvot_mail_no_mailbox_selected)
    val twoPane = rememberPvotWindowLayout().twoPane
    val sceneStrategy = remember(twoPane) {
        MailPaneSceneStrategy<NavKey>(twoPane) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PvotEmptyContent(message = noMailboxSelected)
            }
        }
    }
    fun openList(route: MessageListRoute) {
        backStack.removeAll { it is MessageListRoute }
        backStack.add(route)
    }
    NavDisplay(
        modifier = modifier,
        sceneStrategies = listOf(sceneStrategy),
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
            entry<MailboxesRoute>(metadata = sidebarPane()) {
                MailboxesScreen(
                    onOpenUnifiedInbox = { title ->
                        openList(
                            MessageListRoute.of(title, LocalMessageSearch.createUnifiedSearch(FolderRole.INBOX)),
                        )
                    },
                    onOpenStarredMail = { title -> openList(MessageListRoute.of(title, starredSearch())) },
                    onOpenMailbox = { accountId, folderId, title, subtitle ->
                        openList(MessageListRoute.of(title, folderSearch(accountId, folderId), subtitle))
                    },
                    onNewMessage = { MessageActions.actionCompose(context) },
                    onOpenSettings = onOpenSettings,
                )
            }
            entry<MessageListRoute>(metadata = contentPane()) { route ->
                MessageListScreen(
                    search = route.search,
                    title = route.title,
                    subtitle = route.subtitle,
                    onOpenMessage = { reference -> openMessage(context, reference) },
                    onBack = if (twoPane) null else ({ backStack.removeLastOrNull() }),
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

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fsck.k9.activity.MessageHomeActivity
import com.fsck.k9.activity.compose.MessageActions
import com.fsck.k9.ui.settings.SettingsActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme
import net.thunderbird.feature.account.AccountId
import net.thunderbird.feature.mail.folder.FolderRole
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.prauga.mail.feature.mailboxes.MailboxesScreen

class PvotMailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PvotAppTheme {
                MailboxesScreen(
                    onOpenUnifiedInbox = { openSearch(LocalMessageSearch.createUnifiedSearch(FolderRole.INBOX)) },
                    onOpenMailbox = { accountId, folderId -> openSearch(folderSearch(accountId, folderId)) },
                    onNewMessage = { MessageActions.actionCompose(this) },
                    onOpenSettings = { SettingsActivity.launch(this) },
                )
            }
        }
    }

    private fun openSearch(search: LocalMessageSearch) {
        MessageHomeActivity.actionDisplaySearch(
            context = this,
            search = search,
            noThreading = false,
            newTask = false,
            clearTop = false,
        )
    }

    private fun folderSearch(accountId: AccountId, folderId: Long) = LocalMessageSearch().apply {
        addAccountId(accountId)
        addAllowedFolder(folderId)
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, PvotMailActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

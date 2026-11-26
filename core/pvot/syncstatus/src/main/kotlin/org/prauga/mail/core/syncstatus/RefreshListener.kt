// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.core.syncstatus

import android.content.Context
import app.k9mail.legacy.message.controller.SimpleMessagingListener
import net.thunderbird.feature.account.AccountId

data class RefreshState(val running: Boolean = false, val lastSuccess: Long = 0L)

class RefreshListener(
    private val singleFolder: Boolean,
    private val onDone: (success: Boolean) -> Unit,
) : SimpleMessagingListener() {
    override fun synchronizeMailboxFinished(accountId: AccountId, folderId: Long) {
        if (singleFolder) onDone(true)
    }

    override fun synchronizeMailboxFailed(accountId: AccountId, folderId: Long, message: String?) {
        if (singleFolder) onDone(false)
    }

    override fun checkMailFinished(context: Context?, accountId: AccountId?) {
        if (!singleFolder) onDone(true)
    }
}

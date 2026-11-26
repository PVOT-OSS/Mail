// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.mailboxes

import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureMailboxesModule = module {
    viewModel {
        MailboxesViewModel(
            accountManager = get(),
            displayFolderRepository = get(),
            messageCountsProvider = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
}

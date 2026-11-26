// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import kotlinx.coroutines.Dispatchers
import net.thunderbird.feature.account.AccountId
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureComposerModule = module {
    factory {
        MessageSender(messagingController = get(), generalSettingsManager = get(), ioDispatcher = Dispatchers.IO)
    }
    viewModel { (accountId: AccountId?) ->
        ComposerViewModel(
            accountId = accountId,
            accountManager = get(),
            messageSender = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
}

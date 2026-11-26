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
    factory { ReplyDraftLoader(messagingController = get(), textQuoteCreator = get()) }
    viewModel { (accountId: AccountId?, source: ComposeSource?) ->
        ComposerViewModel(
            accountId = accountId,
            source = source,
            accountManager = get(),
            messageSender = get(),
            replyDraftLoader = get(),
            logger = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
}

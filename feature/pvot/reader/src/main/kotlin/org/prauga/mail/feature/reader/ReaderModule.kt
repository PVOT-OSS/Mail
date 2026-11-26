// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import app.k9mail.legacy.message.controller.MessageReference
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureReaderModule = module {
    factory {
        MessageLoader(
            messagingController = get(),
            extractorFactory = get(),
            htmlSettingsProvider = get(),
            accountManager = get(),
            contactRepository = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
    factory {
        AttachmentOpener(
            context = androidContext(),
            messagingController = get(),
            logger = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
    viewModel { (reference: MessageReference) ->
        ReaderViewModel(reference = reference, loader = get(), messagingController = get(), attachmentOpener = get())
    }
}

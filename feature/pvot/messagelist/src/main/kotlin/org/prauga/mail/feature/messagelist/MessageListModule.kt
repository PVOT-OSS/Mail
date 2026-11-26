// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import java.time.Clock
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureMessageListModule = module {
    factory {
        MessageDateFormatter(
            clock = Clock.systemDefaultZone(),
            locale = Locale.getDefault(),
            yesterday = androidContext().getString(R.string.messagelist_yesterday),
        )
    }
    viewModel { (search: LocalMessageSearch) ->
        MessageListViewModel(
            search = search,
            messageListLoader = get(),
            messageListRepository = get(),
            messagingController = get(),
            dateFormatter = get(),
            accountManager = get(),
            mailChecker = get(),
            clock = Clock.systemDefaultZone(),
            pendingActions = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import kotlinx.coroutines.Dispatchers
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureMessageListModule = module {
    viewModel { (search: LocalMessageSearch) ->
        MessageListViewModel(
            search = search,
            messageListLoader = get(),
            messageListRepository = get(),
            messagingController = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
}

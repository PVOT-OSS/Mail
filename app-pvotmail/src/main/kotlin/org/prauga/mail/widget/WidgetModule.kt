// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.widget

import app.k9mail.feature.widget.message.list.MessageListWidgetConfig
import app.k9mail.feature.widget.unread.UnreadWidgetConfig
import net.thunderbird.feature.widget.message.list.featureWidgetMessageListModule
import org.koin.dsl.module

internal val widgetModule = module {
    includes(featureWidgetMessageListModule)

    single<MessageListWidgetConfig> {
        object : MessageListWidgetConfig {
            override val providerClass = MessageListWidgetProvider::class.java
        }
    }
    single<UnreadWidgetConfig> {
        object : UnreadWidgetConfig {
            override val providerClass = UnreadWidgetProvider::class.java
        }
    }
}

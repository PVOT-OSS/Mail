// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.startup

import app.k9mail.feature.launcher.FeatureLauncherExternalContract
import net.thunderbird.app.common.startup.StartupRouter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.prauga.mail.home.PvotMailActivity

internal val startupModule = module {
    single<StartupRouter> { PvotMailStartupRouter(get(), get()) }
    factory<FeatureLauncherExternalContract.MessageListLauncher> {
        val context = androidContext()
        FeatureLauncherExternalContract.MessageListLauncher { PvotMailActivity.launch(context) }
    }
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail

import net.thunderbird.app.common.FeatureFlagApplication
import org.koin.core.module.Module

class PvotMailApp : FeatureFlagApplication() {
    override val appName: String = "pvotmail"
    override val appVersion: String = BuildConfig.VERSION_NAME
    override val buildType: String = BuildConfig.BUILD_TYPE

    override fun provideAppModule(): Module = appModule
}

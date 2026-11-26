// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.provider

import app.k9mail.core.android.common.provider.NotificationIconResourceProvider
import com.fsck.k9.preferences.FilePrefixProvider
import net.thunderbird.core.common.provider.AppNameProvider
import net.thunderbird.core.common.provider.BrandNameProvider
import net.thunderbird.core.ui.theme.api.FeatureThemeProvider
import net.thunderbird.core.ui.theme.api.ThemeProvider
import net.thunderbird.feature.navigation.changelog.api.ChangelogConfigProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.binds
import org.koin.dsl.module

internal val providerModule = module {
    single {
        PvotMailAppNameProvider(androidContext())
    } binds arrayOf(AppNameProvider::class, BrandNameProvider::class, FilePrefixProvider::class)

    single<ThemeProvider> { PvotMailThemeProvider() }
    single<FeatureThemeProvider> { PvotMailFeatureThemeProvider() }
    single<NotificationIconResourceProvider> { PvotMailNotificationIconProvider() }
    single<ChangelogConfigProvider> { PvotMailChangelogConfigProvider() }
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature

import app.k9mail.feature.migration.launcher.featureMigrationModule
import app.k9mail.feature.onboarding.migration.onboardingMigrationModule
import app.k9mail.feature.telemetry.telemetryModule
import net.thunderbird.android.feature.mail.message.reader.api.css.DefaultCssClassNameProvider
import net.thunderbird.app.common.feature.funding.fundingModule
import net.thunderbird.feature.account.settings.featureAccountSettingsModule
import net.thunderbird.feature.funding.api.FundingSettings
import net.thunderbird.feature.funding.featureFundingModule
import net.thunderbird.feature.mail.message.list.internal.featureMessageListModule
import net.thunderbird.feature.mail.message.reader.api.css.CssClassNameProvider
import net.thunderbird.feature.thundermail.thunderbird.inject.featureThundermailModule
import org.koin.dsl.module
import org.prauga.mail.BuildConfig

internal val featureModule = module {
    includes(featureAccountSettingsModule)
    includes(telemetryModule)
    includes(featureFundingModule)
    includes(onboardingMigrationModule)
    includes(featureMigrationModule)
    includes(featureMessageListModule)
    includes(featureThundermailModule)
    includes(fundingModule)

    single<FundingSettings> { PvotMailFundingSettings() }

    single<CssClassNameProvider> {
        DefaultCssClassNameProvider(
            featureFlagProvider = get(),
            defaultNamespaceClassName = BuildConfig.APPLICATION_ID.replace(oldValue = ".", newValue = "-"),
        )
    }
}

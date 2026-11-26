// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail

import app.k9mail.feature.widget.shortcut.LauncherShortcutActivity
import com.fsck.k9.AppConfig
import com.fsck.k9.DefaultAppConfig
import com.fsck.k9.activity.MessageCompose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.thunderbird.app.common.appCommonModule
import net.thunderbird.core.common.oauth.OAuthConfiguration
import net.thunderbird.core.common.oauth.OAuthConfigurationFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.prauga.mail.core.mailactions.PendingMailActions
import org.prauga.mail.dev.developmentModuleAdditions
import org.prauga.mail.feature.composer.featureComposerModule
import org.prauga.mail.feature.featureModule
import org.prauga.mail.feature.mailboxes.featureMailboxesModule
import org.prauga.mail.feature.messagelist.featureMessageListModule
import org.prauga.mail.feature.reader.featureReaderModule
import org.prauga.mail.featureflag.pvotMailFeatureFlagModule
import org.prauga.mail.provider.providerModule
import org.prauga.mail.startup.startupModule
import org.prauga.mail.widget.MessageListWidgetProvider
import org.prauga.mail.widget.UnreadWidgetProvider
import org.prauga.mail.widget.widgetModule

val appModule = module {
    includes(pvotMailFeatureFlagModule)
    includes(appCommonModule)

    includes(widgetModule)
    includes(featureModule)
    includes(providerModule)
    includes(featureMailboxesModule)
    includes(featureMessageListModule)
    includes(featureComposerModule)
    includes(featureReaderModule)
    includes(startupModule)

    single(named("ClientInfoAppName")) { BuildConfig.CLIENT_INFO_APP_NAME }
    single(named("ClientInfoAppVersion")) { BuildConfig.VERSION_NAME }
    single<AppConfig> { appConfig }
    single<OAuthConfigurationFactory> { noOAuthProviders }
    single { PendingMailActions(scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)) }

    developmentModuleAdditions()
}

private val appConfig = DefaultAppConfig(
    componentsToDisable = listOf(
        MessageCompose::class.java,
        LauncherShortcutActivity::class.java,
        UnreadWidgetProvider::class.java,
        MessageListWidgetProvider::class.java,
    ),
)

private val noOAuthProviders = OAuthConfigurationFactory { emptyMap<List<String>, OAuthConfiguration>() }

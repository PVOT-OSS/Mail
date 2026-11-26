// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.startup

import android.app.Activity
import app.k9mail.feature.launcher.FeatureLauncherActivity
import app.k9mail.feature.launcher.FeatureLauncherTarget
import net.thunderbird.app.common.startup.StartupRouter
import net.thunderbird.core.android.account.LegacyAccountManager
import net.thunderbird.feature.account.settings.api.BackgroundAccountRemover
import org.prauga.mail.home.PvotMailActivity

internal class PvotMailStartupRouter(
    private val accountManager: LegacyAccountManager,
    private val accountRemover: BackgroundAccountRemover,
) : StartupRouter {
    override fun routeToNextScreen(activity: Activity) {
        val accounts = accountManager.findAll()
        accounts.filterNot { it.isFinishedSetup }.forEach { accountRemover.removeAccountAsync(it.id) }

        if (accounts.any { it.isFinishedSetup }) {
            PvotMailActivity.launch(activity)
        } else {
            FeatureLauncherActivity.launch(activity, FeatureLauncherTarget.Onboarding)
        }
    }
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.provider

import net.thunderbird.feature.navigation.changelog.api.ChangelogConfigProvider
import org.prauga.mail.R

internal class PvotMailChangelogConfigProvider : ChangelogConfigProvider {
    override val changelogIndexResId: Int = R.raw.changelog_index
}

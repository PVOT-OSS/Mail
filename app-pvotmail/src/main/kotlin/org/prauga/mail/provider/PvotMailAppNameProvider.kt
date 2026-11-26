// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.provider

import android.content.Context
import com.fsck.k9.preferences.FilePrefixProvider
import net.thunderbird.core.common.provider.AppNameProvider
import net.thunderbird.core.common.provider.BrandNameProvider
import org.prauga.mail.R

internal class PvotMailAppNameProvider(
    context: Context,
) : AppNameProvider, BrandNameProvider, FilePrefixProvider {
    override val appName: String by lazy { context.getString(R.string.app_name) }

    override val brandName: String by lazy { context.getString(R.string.app_name) }

    override val filePrefix: String = "pvotmail"
}

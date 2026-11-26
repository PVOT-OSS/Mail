// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.provider

import net.thunderbird.core.ui.theme.api.ThemeProvider
import org.prauga.mail.R

internal class PvotMailThemeProvider : ThemeProvider {
    override val appThemeResourceId = R.style.Theme_PvotMail_DayNight
    override val appLightThemeResourceId = R.style.Theme_PvotMail_Light
    override val appDarkThemeResourceId = R.style.Theme_PvotMail_Dark
    override val dialogThemeResourceId = R.style.Theme_PvotMail_DayNight_Dialog
    override val translucentDialogThemeResourceId = R.style.Theme_PvotMail_DayNight_Dialog_Translucent
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotDialogActionStyle

@Composable
internal fun SuspiciousLinkDialog(url: String?, link: SuspiciousLink?, onDismiss: () -> Unit) {
    val context = LocalContext.current
    PvotDialog(
        visible = url != null && link != null,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.reader_link_check_title),
        message = link?.let { stringResource(R.string.reader_link_check_message, it.realHost, it.shownHost) },
        destructive = true,
    ) {
        PvotDialogAction(
            label = stringResource(R.string.reader_link_check_cancel),
            onClick = onDismiss,
            style = PvotDialogActionStyle.Primary,
        )
        PvotDialogAction(
            label = stringResource(R.string.reader_link_check_open),
            onClick = {
                url?.let {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(it)).addCategory(Intent.CATEGORY_BROWSABLE),
                        )
                    } catch (_: ActivityNotFoundException) {
                        onDismiss()
                    }
                }
                onDismiss()
            },
        )
    }
}

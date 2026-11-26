// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import android.text.format.Formatter
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.theme.PvotSpacing
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ReaderAttachments(
    attachments: ImmutableList<ReaderAttachment>,
    onOpen: (ReaderAttachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (attachments.isEmpty()) return
    val context = LocalContext.current
    PvotListGroup(
        title = stringResource(R.string.reader_attachments),
        modifier = modifier.padding(PvotSpacing.L),
    ) {
        attachments.forEach { attachment ->
            item {
                PvotListRow(
                    title = attachment.name,
                    supportingText = Formatter.formatShortFileSize(context, attachment.size),
                    leading = {
                        Icon(
                            painter = painterResource(attachment.iconRes()),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = { onOpen(attachment) },
                )
            }
        }
    }
}

@DrawableRes
private fun ReaderAttachment.iconRes(): Int = when {
    mimeType == "application/pdf" -> R.drawable.reader_ic_file_type_pdf
    mimeType.startsWith("image/") -> R.drawable.reader_ic_photo
    else -> R.drawable.reader_ic_file
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.designsystem.components.list.PvotAvatar
import com.prauga.pvot.designsystem.theme.PvotSpacing
import java.text.DateFormat
import java.util.Date

private val AvatarSize = 40.dp
private val LoadingHeight = 96.dp

@Composable
internal fun ExpandedMessage(
    entry: ThreadEntry,
    message: ReaderMessage?,
    showRemoteImages: Boolean,
    canCollapse: Boolean,
    actions: ConversationActions,
) {
    Column {
        MessageHeaderRow(
            entry = entry,
            recipients = message?.recipients,
            modifier = if (canCollapse) Modifier.clickable { actions.onToggle(entry) } else Modifier,
        )
        if (message == null) {
            Box(Modifier.fillMaxWidth().height(LoadingHeight), contentAlignment = Alignment.Center) {
                PvotLoadingContent()
            }
        } else {
            if (message.hasRemoteImages && !showRemoteImages) {
                RemoteImagesBar(onLoad = { actions.onLoadRemoteImages(entry) })
            }
            ReaderBody(
                message = message,
                showRemoteImages = showRemoteImages,
                onSuspiciousLink = actions.onSuspiciousLink,
                modifier = Modifier.fillMaxWidth(),
            )
            ReaderAttachments(
                attachments = message.attachments,
                onOpen = { attachment -> actions.onOpenAttachment(entry, attachment) },
            )
        }
        Separator()
    }
}

@Composable
internal fun CollapsedMessage(entry: ThreadEntry, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.M),
            horizontalArrangement = Arrangement.spacedBy(PvotSpacing.M),
        ) {
            PvotAvatar(name = entry.senderName, colorKey = entry.senderAddress, size = AvatarSize)
            Column(Modifier.weight(1f)) {
                SenderLine(entry)
                Text(
                    text = entry.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Separator()
    }
}

@Composable
private fun MessageHeaderRow(entry: ThreadEntry, recipients: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.M),
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PvotAvatar(name = entry.senderName, colorKey = entry.senderAddress, size = AvatarSize)
        Column(Modifier.weight(1f)) {
            SenderLine(entry)
            if (!recipients.isNullOrEmpty()) {
                Text(
                    text = stringResource(R.string.reader_to, recipients),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SenderLine(entry: ThreadEntry) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = entry.senderName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (entry.isUnread) FontWeight.Bold else FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(entry.sentAt)),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Separator(thickness: Dp = Dp.Hairline) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PvotSpacing.L)
            .height(thickness)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.prauga.pvot.designsystem.theme.PvotSpacing

private val UnreadDotSize = 10.dp
private val MetaIconSize = 16.dp

@Composable
internal fun MessageRowContent(row: MessageRow, onClick: () -> Unit) {
    val unreadLabel = stringResource(R.string.messagelist_unread)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = PvotSpacing.S, vertical = PvotSpacing.M),
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        Box(modifier = Modifier.padding(top = PvotSpacing.S).size(UnreadDotSize)) {
            if (row.isUnread) {
                Box(
                    modifier = Modifier
                        .size(UnreadDotSize)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .semantics { contentDescription = unreadLabel },
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.sender,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (row.isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                MetaIcons(row)
                Text(
                    text = row.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = PvotSpacing.XS),
                )
            }
            Text(
                text = row.subject,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = row.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MetaIcons(row: MessageRow) {
    Row(horizontalArrangement = Arrangement.spacedBy(PvotSpacing.XS)) {
        if (row.hasAttachments) {
            Icon(
                painter = painterResource(R.drawable.messagelist_ic_paperclip),
                contentDescription = stringResource(R.string.messagelist_has_attachments),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(MetaIconSize),
            )
        }
        if (row.isStarred) {
            Icon(
                painter = painterResource(R.drawable.messagelist_ic_star),
                contentDescription = stringResource(R.string.messagelist_starred),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(MetaIconSize),
            )
        }
    }
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotGlassIconButton
import com.prauga.pvot.designsystem.theme.PvotSpacing

@Composable
internal fun ReaderBottomBar(isFlagged: Boolean, actions: ReaderBarActions, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        BarButton(R.drawable.reader_ic_trash, R.string.reader_delete, actions.onDelete)
        BarButton(R.drawable.reader_ic_archive, R.string.reader_archive, actions.onArchive)
        Spacer(Modifier.weight(1f))
        BarButton(
            icon = R.drawable.reader_ic_flag,
            label = if (isFlagged) R.string.reader_unflag else R.string.reader_flag,
            onClick = actions.onToggleFlag,
            highlighted = isFlagged,
        )
        BarButton(R.drawable.reader_ic_mail, R.string.reader_mark_unread, actions.onMarkUnread)
    }
}

@Composable
private fun BarButton(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    onClick: () -> Unit,
    highlighted: Boolean = false,
) {
    PvotGlassIconButton(onClick = onClick, backdrop = null) {
        Icon(
            painter = painterResource(icon),
            contentDescription = stringResource(label),
            tint = if (highlighted) MaterialTheme.colorScheme.primary else LocalContentColor.current,
        )
    }
}

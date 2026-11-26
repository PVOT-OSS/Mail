// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotBottomSheet
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.components.list.PvotSwitchRow
import com.prauga.pvot.designsystem.theme.PvotSpacing
import com.prauga.pvot.designsystem.R as DesignSystemR

@Composable
internal fun FiltersSheet(
    visible: Boolean,
    filter: FilterState,
    onDismiss: () -> Unit,
    onEvent: (MessageListEvent) -> Unit,
) {
    PvotBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.messagelist_filters),
    ) {
        PvotListGroup(
            title = stringResource(R.string.messagelist_filters_include),
            modifier = Modifier.padding(bottom = PvotSpacing.L),
        ) {
            MessageFilter.entries.forEach { option ->
                item {
                    PvotListRow(
                        title = stringResource(option.labelRes()),
                        leading = { FilterIcon(option.iconRes()) },
                        trailing = if (option in filter.include) {
                            { CheckIcon() }
                        } else {
                            null
                        },
                        onClick = { onEvent(MessageListEvent.ToggleInclude(option)) },
                    )
                }
            }
        }
        PvotListGroup(modifier = Modifier.padding(bottom = PvotSpacing.L)) {
            item {
                PvotSwitchRow(
                    title = stringResource(R.string.messagelist_filter_only_attachments),
                    checked = filter.onlyWithAttachments,
                    onCheckedChange = { onEvent(MessageListEvent.SetOnlyWithAttachments(it)) },
                    icon = { FilterIcon(R.drawable.messagelist_ic_paperclip) },
                )
            }
        }
    }
}

@Composable
private fun FilterIcon(@DrawableRes icon: Int) {
    Icon(painter = painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
}

@Composable
private fun CheckIcon() {
    Icon(
        painter = painterResource(DesignSystemR.drawable.pvot_ic_check),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
    )
}

internal fun MessageFilter.labelRes(): Int = when (this) {
    MessageFilter.UNREAD -> R.string.messagelist_filter_unread
    MessageFilter.FLAGGED -> R.string.messagelist_filter_flagged
}

@DrawableRes
private fun MessageFilter.iconRes(): Int = when (this) {
    MessageFilter.UNREAD -> R.drawable.messagelist_ic_mail
    MessageFilter.FLAGGED -> R.drawable.messagelist_ic_flag
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import com.prauga.pvot.designsystem.components.PvotGlassIconButton
import com.prauga.pvot.designsystem.components.PvotGlassIconButtonDefaults
import com.prauga.pvot.designsystem.components.PvotGlassSurface
import com.prauga.pvot.designsystem.components.textfield.PvotTextField
import com.prauga.pvot.designsystem.effects.PvotGlassDefaults
import com.prauga.pvot.designsystem.theme.PvotSpacing
import com.prauga.pvot.designsystem.R as DesignSystemR

@Suppress("LongParameterList")
@Composable
internal fun MessageListBottomBar(
    filter: FilterState,
    query: String,
    searching: Boolean,
    onSearchingChange: (Boolean) -> Unit,
    onEvent: (MessageListEvent) -> Unit,
    onShowFilters: () -> Unit,
    onNewMessage: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        if (searching) {
            SearchField(
                query = query,
                onQueryChange = { onEvent(MessageListEvent.Search(it)) },
                modifier = Modifier.weight(1f),
            )
            PvotGlassIconButton(onClick = { onSearchingChange(false) }) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.pvot_ic_x),
                    contentDescription = stringResource(R.string.messagelist_search_close),
                )
            }
        } else {
            FilterButton(filter = filter, onClick = { onEvent(MessageListEvent.ToggleFilter) })
            if (filter.enabled) {
                FilterLabel(filter = filter, onClick = onShowFilters)
                Spacer(Modifier.weight(1f))
                PvotGlassIconButton(onClick = { onSearchingChange(true) }) { SearchIcon() }
            } else {
                SearchPill(onClick = { onSearchingChange(true) }, modifier = Modifier.weight(1f))
            }
            PvotGlassIconButton(onClick = onNewMessage) {
                Icon(
                    painter = painterResource(R.drawable.messagelist_ic_edit),
                    contentDescription = stringResource(R.string.messagelist_new_message),
                )
            }
        }
    }
}

@Composable
private fun SearchPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PvotGlassSurface(
        onClick = onClick,
        modifier = modifier.height(PvotGlassIconButtonDefaults.Size),
        contentAlignment = Alignment.CenterStart,
        contentPadding = PaddingValues(horizontal = PvotSpacing.L),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchIcon()
            Text(
                text = stringResource(R.string.messagelist_search),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = PvotSpacing.S),
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    PvotTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.messagelist_search),
        leadingIcon = { SearchIcon() },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = modifier.focusRequester(focusRequester),
    )
}

@Composable
private fun SearchIcon() {
    Icon(
        painter = painterResource(R.drawable.messagelist_ic_search),
        contentDescription = stringResource(R.string.messagelist_search),
    )
}

@Composable
private fun FilterButton(filter: FilterState, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val stateLabel =
        stringResource(if (filter.enabled) R.string.messagelist_filter_on else R.string.messagelist_filter_off)
    PvotGlassIconButton(
        onClick = onClick,
        style = if (filter.enabled) {
            PvotGlassDefaults.Style.copy(
                fillColor = colors.primary,
            )
        } else {
            PvotGlassDefaults.Style
        },
        contentColor = if (filter.enabled) colors.onPrimary else colors.onSurface,
        modifier = Modifier.semantics { stateDescription = stateLabel },
    ) {
        Icon(
            painter = painterResource(R.drawable.messagelist_ic_filter_2),
            contentDescription = stringResource(R.string.messagelist_filter),
        )
    }
}

@Composable
private fun FilterLabel(filter: FilterState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val names = filter.include.map { stringResource(it.labelRes()) } +
        listOfNotNull(stringResource(R.string.messagelist_filter_attachments).takeIf { filter.onlyWithAttachments })
    Column(modifier.clickable(onClickLabel = stringResource(R.string.messagelist_filters), onClick = onClick)) {
        Text(
            text = stringResource(R.string.messagelist_filtered_by),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = names.joinToString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

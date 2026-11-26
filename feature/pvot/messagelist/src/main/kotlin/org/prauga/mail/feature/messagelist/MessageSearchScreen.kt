// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.k9mail.legacy.message.controller.MessageReference
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.designsystem.components.PvotGlassIconButton
import com.prauga.pvot.designsystem.components.PvotGlassSurface
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.textfield.PvotTextField
import com.prauga.pvot.designsystem.theme.PvotSpacing
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.prauga.pvot.designsystem.R as DesignSystemR

@Composable
fun MessageSearchScreen(
    search: LocalMessageSearch,
    scopeTitle: String,
    onOpenMessage: (MessageReference) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageListViewModel = koinViewModel { parametersOf(search) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PvotScreenScaffold(
        modifier = modifier,
        bottomBar = {
            SearchBar(
                query = state.query,
                onQueryChange = { viewModel.onEvent(MessageListEvent.Search(it)) },
                onClose = onClose,
            )
        },
    ) { padding ->
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            when {
                state.query.isBlank() -> Centered(padding.calculateBottomPadding()) {
                    PvotEmptyContent(message = stringResource(R.string.messagelist_search_in, scopeTitle))
                }

                state.rows.isEmpty() -> Centered(padding.calculateBottomPadding()) {
                    PvotEmptyContent(message = stringResource(R.string.messagelist_search_no_results))
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + PvotSpacing.S,
                        bottom = padding.calculateBottomPadding() + PvotSpacing.L,
                    ),
                ) {
                    items(state.rows, key = { it.key }) { row ->
                        Box(Modifier.padding(horizontal = PvotSpacing.S)) {
                            MessageRowContent(row = row, onClick = { onOpenMessage(row.reference) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        PvotGlassSurface(modifier = Modifier.weight(1f), rim = false, contentPadding = PaddingValues()) {
            PvotTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.messagelist_search),
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.messagelist_ic_search), contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        }
        PvotGlassIconButton(onClick = onClose) {
            Icon(
                painter = painterResource(DesignSystemR.drawable.pvot_ic_x),
                contentDescription = stringResource(R.string.messagelist_search_close),
            )
        }
    }
}

@Composable
private fun Centered(bottom: Dp, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(bottom = bottom), contentAlignment = Alignment.Center) { content() }
}

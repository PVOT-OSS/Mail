// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.k9mail.legacy.message.controller.MessageReference
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.components.list.PvotSwipeAction
import com.prauga.pvot.designsystem.components.list.PvotSwipeRow
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import kotlinx.coroutines.launch
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MessageListScreen(
    search: LocalMessageSearch,
    title: String,
    onOpenMessage: (MessageReference) -> Unit,
    onBack: (() -> Unit)?,
    viewModel: MessageListViewModel = koinViewModel(key = title) { parametersOf(search) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val archived = stringResource(R.string.messagelist_archived)
    val undo = stringResource(R.string.messagelist_undo)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MessageListEffect.ShowArchiveUndo -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = archived,
                        actionLabel = undo,
                        duration = SnackbarDuration.Short,
                    )
                    viewModel.onEvent(
                        if (result == SnackbarResult.ActionPerformed) {
                            MessageListEvent.UndoArchive(effect.key)
                        } else {
                            MessageListEvent.CommitArchive(effect.key)
                        },
                    )
                }
            }
        }
    }
    MessageListContent(
        snackbarHostState = snackbarHostState,
        state = state,
        title = title,
        onOpenMessage = onOpenMessage,
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun MessageListContent(
    snackbarHostState: SnackbarHostState,
    state: MessageListUiState,
    title: String,
    onOpenMessage: (MessageReference) -> Unit,
    onBack: (() -> Unit)?,
    onEvent: (MessageListEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val compact = rememberPvotTopBarCompact(listState)
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    PvotScreenScaffold(
        topBar = { PvotTopBar(title = title, onBack = onBack, compact = compact) },
        snackbarHost = {
            PvotSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding().padding(horizontal = PvotSpacing.M),
            )
        },
    ) { padding ->
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            when {
                state.isLoading -> CenteredBox(padding.calculateTopPadding()) { PvotLoadingContent() }

                state.rows.isEmpty() -> CenteredBox(padding.calculateTopPadding()) {
                    PvotEmptyContent(message = stringResource(R.string.messagelist_empty))
                }

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + PvotSpacing.S,
                        bottom = bottom + PvotSpacing.L,
                    ),
                ) {
                    items(state.rows, key = { it.key }) { row ->
                        SwipeableMessageRow(
                            row = row,
                            onOpen = { onOpenMessage(row.reference) },
                            onEvent = onEvent,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeableMessageRow(
    row: MessageRow,
    onOpen: () -> Unit,
    onEvent: (MessageListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    PvotSwipeRow(
        modifier = modifier.padding(horizontal = PvotSpacing.S),
        startAction = PvotSwipeAction(
            label = stringResource(
                if (row.isUnread) R.string.messagelist_action_mark_read else R.string.messagelist_action_mark_unread,
            ),
            icon = painterResource(
                if (row.isUnread) R.drawable.messagelist_ic_mail_opened else R.drawable.messagelist_ic_mail,
            ),
            onSwipe = { onEvent(MessageListEvent.ToggleRead(row)) },
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
        ),
        endAction = PvotSwipeAction(
            label = stringResource(R.string.messagelist_action_archive),
            icon = painterResource(R.drawable.messagelist_ic_archive),
            onSwipe = { onEvent(MessageListEvent.Archive(row)) },
            containerColor = colors.tertiary,
            contentColor = colors.onTertiary,
            removesRow = true,
        ),
    ) {
        MessageRowContent(row = row, onClick = onOpen)
    }
}

@Composable
private fun CenteredBox(top: Dp, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(top = top),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

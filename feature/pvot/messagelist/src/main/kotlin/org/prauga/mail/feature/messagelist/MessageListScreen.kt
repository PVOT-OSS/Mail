// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.k9mail.legacy.message.controller.MessageReference
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.designsystem.components.PvotGlassIconButton
import com.prauga.pvot.designsystem.components.PvotPullToRefreshBox
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.components.list.PvotSwipeAction
import com.prauga.pvot.designsystem.components.list.PvotSwipeRow
import com.prauga.pvot.designsystem.effects.PvotGlassDefaults
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import kotlinx.coroutines.launch
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.prauga.mail.core.syncstatus.lastUpdatedText

private const val SUBTITLE_SEPARATOR = " · "

@Composable
fun MessageListScreen(
    search: LocalMessageSearch,
    title: String,
    subtitle: String?,
    onOpenMessage: (MessageReference) -> Unit,
    onNewMessage: () -> Unit,
    onOpenSearch: () -> Unit,
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
        subtitle = subtitle,
        onOpenMessage = onOpenMessage,
        onNewMessage = onNewMessage,
        onOpenSearch = onOpenSearch,
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun MessageListContent(
    snackbarHostState: SnackbarHostState,
    state: MessageListUiState,
    title: String,
    subtitle: String?,
    onOpenMessage: (MessageReference) -> Unit,
    onNewMessage: () -> Unit,
    onOpenSearch: () -> Unit,
    onBack: (() -> Unit)?,
    onEvent: (MessageListEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    FiltersSheet(visible = showFilters, filter = state.filter, onDismiss = { showFilters = false }, onEvent = onEvent)
    val compact = rememberPvotTopBarCompact(listState)
    PvotScreenScaffold(
        topBar = { ListTopBar(title, subtitle, state.lastUpdated, onBack, compact) },
        bottomBar = {
            MessageListBottomBar(
                filter = state.filter,
                onOpenSearch = onOpenSearch,
                onEvent = onEvent,
                onShowFilters = { showFilters = true },
                onNewMessage = onNewMessage,
            )
        },
        snackbarHost = {
            PvotSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(horizontal = PvotSpacing.M),
            )
        },
    ) { padding ->
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            when {
                state.isLoading -> CenteredBox(padding.calculateTopPadding()) { PvotLoadingContent() }

                state.rows.isEmpty() -> CenteredBox(padding.calculateTopPadding()) {
                    PvotEmptyContent(message = stringResource(R.string.messagelist_empty))
                }

                else -> PvotPullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onEvent(MessageListEvent.Refresh) },
                    indicatorPadding = PaddingValues(top = padding.calculateTopPadding()),
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            top = padding.calculateTopPadding() + PvotSpacing.S,
                            bottom = padding.calculateBottomPadding() + PvotSpacing.L,
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
}

@Composable
private fun ListTopBar(title: String, subtitle: String?, lastUpdated: Long, onBack: (() -> Unit)?, compact: Boolean) {
    PvotTopBar(
        title = title,
        onBack = onBack,
        compact = compact,
        subtitle = listOfNotNull(subtitle, lastUpdatedText(lastUpdated)).joinToString(SUBTITLE_SEPARATOR).ifEmpty {
            null
        },
    )
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

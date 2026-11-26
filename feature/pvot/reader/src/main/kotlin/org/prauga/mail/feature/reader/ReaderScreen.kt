// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.k9mail.legacy.message.controller.MessageReference
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReaderScreen(
    reference: MessageReference,
    onBack: (() -> Unit)?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = koinViewModel { parametersOf(reference) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var linkToCheck by remember { mutableStateOf<Pair<String, SuspiciousLink>?>(null) }
    SuspiciousLinkDialog(url = linkToCheck?.first, link = linkToCheck?.second, onDismiss = { linkToCheck = null })
    val snackbarHostState = remember { SnackbarHostState() }
    val attachmentFailed = stringResource(R.string.reader_attachment_failed)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ReaderEffect.AttachmentFailed -> snackbarHostState.showSnackbar(attachmentFailed)
                ReaderEffect.Close -> onClose()
            }
        }
    }
    PvotScreenScaffold(
        modifier = modifier,
        snackbarHost = { PvotSnackbarHost(snackbarHostState) },
        topBar = { PvotTopBar(title = "", onBack = onBack, compact = true) },
        bottomBar = {
            if (state.entries.isNotEmpty()) {
                ReaderBottomBar(
                    isFlagged = state.isFlagged,
                    actions = ReaderBarActions(
                        onArchive = viewModel::archive,
                        onDelete = viewModel::delete,
                        onToggleFlag = viewModel::toggleFlag,
                        onMarkUnread = viewModel::markUnread,
                    ),
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PvotLoadingContent()
            }

            state.failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PvotEmptyContent(message = stringResource(R.string.reader_failed))
            }

            else -> Conversation(
                state = state,
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + PvotSpacing.L,
                ),
                actions = ConversationActions(
                    onToggle = viewModel::toggle,
                    onLoadRemoteImages = viewModel::loadRemoteImages,
                    onOpenAttachment = viewModel::openAttachment,
                    onSuspiciousLink = { url, link -> linkToCheck = url to link },
                ),
            )
        }
    }
}

internal class ReaderBarActions(
    val onArchive: () -> Unit,
    val onDelete: () -> Unit,
    val onToggleFlag: () -> Unit,
    val onMarkUnread: () -> Unit,
)

internal class ConversationActions(
    val onToggle: (ThreadEntry) -> Unit,
    val onLoadRemoteImages: (ThreadEntry) -> Unit,
    val onOpenAttachment: (ThreadEntry, ReaderAttachment) -> Unit,
    val onSuspiciousLink: (String, SuspiciousLink) -> Unit,
)

@Composable
private fun Conversation(state: ReaderUiState, contentPadding: PaddingValues, actions: ConversationActions) {
    val canCollapse = state.entries.size > 1
    LazyColumn(contentPadding = contentPadding) {
        item(key = "subject") {
            Text(
                text = state.subject?.ifBlank { null } ?: stringResource(R.string.reader_no_subject),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.S),
            )
        }
        items(state.entries, key = { it.key }) { entry ->
            if (entry.key in state.expanded) {
                ExpandedMessage(
                    entry = entry,
                    message = state.messages[entry.key],
                    showRemoteImages = entry.key in state.remoteImagesShown,
                    canCollapse = canCollapse,
                    actions = actions,
                )
            } else {
                CollapsedMessage(entry = entry, onClick = { actions.onToggle(entry) })
            }
        }
    }
}

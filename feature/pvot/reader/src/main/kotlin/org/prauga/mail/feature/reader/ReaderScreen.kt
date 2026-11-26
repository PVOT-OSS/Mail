// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.k9mail.legacy.message.controller.MessageReference
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.list.PvotAvatar
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.theme.PvotSpacing
import java.text.DateFormat
import java.util.Date
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val AvatarSize = 44.dp

@Composable
fun ReaderScreen(
    reference: MessageReference,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = koinViewModel { parametersOf(reference) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PvotScreenScaffold(
        modifier = modifier,
        topBar = { PvotTopBar(title = "", onBack = onBack, compact = true) },
    ) { padding ->
        when (val current = state) {
            ReaderUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PvotLoadingContent()
            }

            ReaderUiState.Failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PvotEmptyContent(message = stringResource(R.string.reader_failed))
            }

            is ReaderUiState.Loaded -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
            ) {
                ReaderHeader(current.message)
                ReaderBody(current.message, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ReaderHeader(message: ReaderMessage) {
    Column(Modifier.padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.S)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PvotSpacing.M),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PvotAvatar(name = message.senderName, colorKey = message.senderAddress, size = AvatarSize)
            Column(Modifier.weight(1f)) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (message.recipients.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.reader_to, message.recipients),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Text(
                text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(message.sentAt)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = message.subject.ifBlank { stringResource(R.string.reader_no_subject) },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = PvotSpacing.M),
        )
    }
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.designsystem.components.PvotGlassIconButton
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.effects.PvotGlassDefaults
import com.prauga.pvot.designsystem.theme.PvotSpacing
import net.thunderbird.feature.account.AccountId
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.prauga.pvot.designsystem.R as DesignSystemR

@Composable
fun ComposerScreen(
    accountId: AccountId?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ComposerViewModel = koinViewModel { parametersOf(accountId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sendFailed = stringResource(R.string.composer_send_failed)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ComposerEffect.Sent -> onClose()
                is ComposerEffect.Failed -> snackbarHostState.showSnackbar(sendFailed)
            }
        }
    }
    PvotScreenScaffold(
        modifier = modifier,
        snackbarHost = { PvotSnackbarHost(snackbarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PvotSpacing.L),
        ) {
            ComposerHeader(canSend = state.canSend, onClose = onClose, onSend = {
                viewModel.onEvent(ComposerEvent.Send)
            })
            ComposerFields(state = state, onEvent = viewModel::onEvent)
        }
    }
}

@Composable
private fun ComposerHeader(canSend: Boolean, onClose: () -> Unit, onSend: () -> Unit) {
    Column {
        val colors = MaterialTheme.colorScheme
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = PvotSpacing.S),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PvotGlassIconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.pvot_ic_x),
                    contentDescription = stringResource(R.string.composer_close),
                )
            }
            PvotGlassIconButton(
                onClick = onSend,
                enabled = canSend,
                style = if (canSend) {
                    PvotGlassDefaults.Style.copy(
                        fillColor = colors.primary,
                    )
                } else {
                    PvotGlassDefaults.Style
                },
                contentColor = if (canSend) colors.onPrimary else colors.onSurfaceVariant,
            ) {
                Icon(
                    painter = painterResource(R.drawable.composer_ic_arrow_up),
                    contentDescription = stringResource(R.string.composer_send),
                )
            }
        }
        Text(
            text = stringResource(R.string.composer_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = PvotSpacing.M),
        )
    }
}

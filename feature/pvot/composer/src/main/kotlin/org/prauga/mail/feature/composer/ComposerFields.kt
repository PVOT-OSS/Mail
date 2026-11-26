// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prauga.pvot.designsystem.components.PvotMenu
import com.prauga.pvot.designsystem.components.PvotMenuItem
import com.prauga.pvot.designsystem.theme.PvotSpacing
import com.prauga.pvot.designsystem.R as DesignSystemR

private val FieldMinHeight = 52.dp
private const val BODY_MIN_LINES = 8

@Composable
internal fun ComposerFields(state: ComposerUiState, onEvent: (ComposerEvent) -> Unit) {
    Column {
        FieldRow(stringResource(R.string.composer_to)) {
            FieldInput(state.to, { onEvent(ComposerEvent.ChangeTo(it)) }, KeyboardType.Email)
        }
        if (state.showCcBcc) {
            FieldRow(stringResource(R.string.composer_cc)) {
                FieldInput(state.cc, { onEvent(ComposerEvent.ChangeCc(it)) }, KeyboardType.Email)
            }
            FieldRow(stringResource(R.string.composer_bcc)) {
                FieldInput(state.bcc, { onEvent(ComposerEvent.ChangeBcc(it)) }, KeyboardType.Email)
            }
            FromRow(state = state, onEvent = onEvent)
        } else {
            FieldRow(
                label = stringResource(R.string.composer_cc_bcc_from, state.from),
                modifier = Modifier.clickable { onEvent(ComposerEvent.ShowCcBcc) },
            ) {}
        }
        FieldRow(stringResource(R.string.composer_subject)) {
            FieldInput(state.subject, { onEvent(ComposerEvent.ChangeSubject(it)) }, KeyboardType.Text)
        }
        BasicTextField(
            value = state.body,
            onValueChange = { onEvent(ComposerEvent.ChangeBody(it)) },
            minLines = BODY_MIN_LINES,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth().padding(vertical = PvotSpacing.M),
        )
        state.signature?.let { signature ->
            Text(
                text = signature,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = PvotSpacing.L),
            )
        }
        state.quotedText?.let { quoted ->
            Text(
                text = quoted.trimEnd(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = PvotSpacing.L),
            )
        }
    }
}

@Composable
private fun FromRow(state: ComposerUiState, onEvent: (ComposerEvent) -> Unit) {
    FieldRow(stringResource(R.string.composer_from)) {
        Text(
            text = state.from,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (state.fromAddresses.size > 1) {
            PvotMenu(
                entries = state.fromAddresses.mapIndexed { index, address ->
                    PvotMenuItem(label = address, onClick = { onEvent(ComposerEvent.SelectFrom(index)) })
                },
                contentDescription = stringResource(R.string.composer_choose_from),
                icon = DesignSystemR.drawable.pvot_ic_chevron_right,
            )
        }
    }
}

@Composable
private fun FieldRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val divider = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = FieldMinHeight)
            .drawBehind {
                drawLine(divider, Offset(0f, size.height), Offset(size.width, size.height), Dp.Hairline.toPx())
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = PvotSpacing.S),
        )
        content()
    }
}

@Composable
private fun RowScope.FieldInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        modifier = Modifier.weight(1f),
    )
}

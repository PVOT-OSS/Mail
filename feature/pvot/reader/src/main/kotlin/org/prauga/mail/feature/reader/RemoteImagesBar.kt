// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotCard
import com.prauga.pvot.designsystem.components.button.PvotButton
import com.prauga.pvot.designsystem.components.button.PvotButtonStyle
import com.prauga.pvot.designsystem.theme.PvotSpacing

@Composable
internal fun RemoteImagesBar(onLoad: () -> Unit, modifier: Modifier = Modifier) {
    PvotCard(modifier = modifier.fillMaxWidth().padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.S)) {
        Row(
            modifier = Modifier.padding(PvotSpacing.M),
            horizontalArrangement = Arrangement.spacedBy(PvotSpacing.M),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.reader_remote_images_blocked),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            PvotButton(
                text = stringResource(R.string.reader_load_images),
                onClick = onLoad,
                style = PvotButtonStyle.Tonal,
            )
        }
    }
}

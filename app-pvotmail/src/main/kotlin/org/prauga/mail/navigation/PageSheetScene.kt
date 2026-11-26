// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.prauga.pvot.designsystem.components.PvotPageSheet
import com.prauga.pvot.designsystem.components.PvotPageSheetState
import com.prauga.pvot.designsystem.components.rememberPvotPageSheetState

object PageSheet : NavMetadataKey<Boolean>

fun pageSheet() = metadata { put(PageSheet, true) }

private class PageSheetScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val onBack: () -> Unit,
) : OverlayScene<T> {
    override val entries: List<NavEntry<T>> = listOf(entry)

    private var sheetState: PvotPageSheetState? = null

    override val content: @Composable () -> Unit = {
        val state = rememberPvotPageSheetState()
        sheetState = state
        PvotPageSheet(onDismissRequest = onBack, state = state) { entry.Content() }
    }

    override suspend fun onRemove() {
        sheetState?.hide()
    }
}

class PageSheetSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val last = entries.lastOrNull()?.takeIf { it.metadata.contains(PageSheet) } ?: return null
        return PageSheetScene(
            key = last.contentKey,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            entry = last,
            onBack = onBack,
        )
    }
}

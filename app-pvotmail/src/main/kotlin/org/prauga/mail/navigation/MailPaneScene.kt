// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.prauga.pvot.designsystem.adaptive.PvotTwoPane

object SidebarPane : NavMetadataKey<Boolean>

object ContentPane : NavMetadataKey<Boolean>

fun sidebarPane() = metadata { put(SidebarPane, true) }

fun contentPane() = metadata { put(ContentPane, true) }

private class MailPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    private val sidebar: NavEntry<T>,
    private val detail: NavEntry<T>?,
    private val placeholder: @Composable () -> Unit,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOfNotNull(sidebar, detail)

    override val content: @Composable () -> Unit = {
        PvotTwoPane(
            first = { sidebar.Content() },
            second = { detail?.Content() ?: placeholder() },
        )
    }
}

class MailPaneSceneStrategy<T : Any>(
    private val twoPane: Boolean,
    private val placeholder: @Composable () -> Unit,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val last = entries.lastOrNull()
        val sidebar = entries.findLast { it.metadata.contains(SidebarPane) }
        val detail = last?.takeIf { it.metadata.contains(ContentPane) }
        val showsPanes = last === sidebar || detail != null
        return sidebar?.takeIf { twoPane && showsPanes }?.let {
            MailPaneScene(SCENE_KEY, entries.dropLast(1), it, detail, placeholder)
        }
    }

    private companion object {
        const val SCENE_KEY = "mail-panes"
    }
}

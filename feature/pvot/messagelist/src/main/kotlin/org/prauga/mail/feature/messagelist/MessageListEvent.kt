// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

sealed interface MessageListEvent {
    data class Archive(val row: MessageRow) : MessageListEvent
    data class ToggleRead(val row: MessageRow) : MessageListEvent
    data object ToggleFilter : MessageListEvent
    data object Refresh : MessageListEvent
    data class Search(val query: String) : MessageListEvent
    data class ToggleInclude(val filter: MessageFilter) : MessageListEvent
    data class SetOnlyWithAttachments(val value: Boolean) : MessageListEvent
    data class UndoArchive(val key: Long) : MessageListEvent
    data class CommitArchive(val key: Long) : MessageListEvent
}

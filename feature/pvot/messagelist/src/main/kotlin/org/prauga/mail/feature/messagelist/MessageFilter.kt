// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

enum class MessageFilter {
    UNREAD,
    FLAGGED,
    ATTACHMENTS,
    ;

    fun matches(row: MessageRow): Boolean = when (this) {
        UNREAD -> row.isUnread
        FLAGGED -> row.isStarred
        ATTACHMENTS -> row.hasAttachments
    }
}

data class FilterState(
    val enabled: Boolean = false,
    val filters: Set<MessageFilter> = setOf(MessageFilter.UNREAD),
)

internal fun List<MessageRow>.filteredBy(state: FilterState): List<MessageRow> =
    if (!state.enabled || state.filters.isEmpty()) {
        this
    } else {
        filter { row -> state.filters.any { it.matches(row) } }
    }

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

enum class MessageFilter {
    UNREAD,
    FLAGGED,
    ;

    fun matches(row: MessageRow): Boolean = when (this) {
        UNREAD -> row.isUnread
        FLAGGED -> row.isStarred
    }
}

data class FilterState(
    val enabled: Boolean = false,
    val include: Set<MessageFilter> = setOf(MessageFilter.UNREAD),
    val onlyWithAttachments: Boolean = false,
)

internal fun List<MessageRow>.filteredBy(state: FilterState): List<MessageRow> =
    if (!state.enabled) {
        this
    } else {
        filter { row ->
            (state.include.isEmpty() || state.include.any { it.matches(row) }) &&
                (!state.onlyWithAttachments || row.hasAttachments)
        }
    }

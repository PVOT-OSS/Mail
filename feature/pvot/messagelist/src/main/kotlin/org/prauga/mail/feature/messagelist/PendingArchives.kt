// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PendingArchives {
    private val pending = MutableStateFlow<Map<Long, MessageRow>>(emptyMap())

    val keys: StateFlow<Map<Long, MessageRow>> = pending.asStateFlow()

    fun add(row: MessageRow) {
        pending.update { it + (row.key to row) }
    }

    fun undo(key: Long): Boolean = take(key) != null

    fun take(key: Long): MessageRow? {
        var taken: MessageRow? = null
        pending.update { current ->
            taken = current[key]
            current - key
        }
        return taken
    }

    fun takeAll(): List<MessageRow> {
        var all: List<MessageRow> = emptyList()
        pending.update { current ->
            all = current.values.toList()
            emptyMap()
        }
        return all
    }
}

internal fun List<MessageRow>.without(pending: Map<Long, MessageRow>): List<MessageRow> =
    if (pending.isEmpty()) this else filterNot { it.key in pending }

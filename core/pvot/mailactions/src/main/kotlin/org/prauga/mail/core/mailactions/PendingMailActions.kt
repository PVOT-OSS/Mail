// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.core.mailactions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val DEFAULT_COMMIT_DELAY_MILLIS = 8_000L

enum class MailActionKind {
    ARCHIVE,
    DELETE,
}

data class PendingMailAction(val id: Long, val kind: MailActionKind, val messageKeys: Set<Long>)

class PendingMailActions(
    private val scope: CoroutineScope,
    private val commitDelayMillis: Long = DEFAULT_COMMIT_DELAY_MILLIS,
) {
    private class Entry(val action: PendingMailAction, val perform: () -> Unit, var timeout: Job? = null)

    private val entries = MutableStateFlow<List<Entry>>(emptyList())
    private val actions = MutableStateFlow<List<PendingMailAction>>(emptyList())
    private var nextId = 0L

    val pending: StateFlow<List<PendingMailAction>> = actions.asStateFlow()

    fun add(kind: MailActionKind, messageKeys: Set<Long>, perform: () -> Unit): PendingMailAction {
        val action = PendingMailAction(id = nextId++, kind = kind, messageKeys = messageKeys)
        val entry = Entry(action, perform)
        update { it + entry }
        entry.timeout = scope.launch {
            delay(commitDelayMillis)
            commit(action.id)
        }
        return action
    }

    fun undo(id: Long) {
        take(id)?.timeout?.cancel()
    }

    fun commit(id: Long) {
        take(id)?.let { entry ->
            entry.timeout?.cancel()
            entry.perform()
        }
    }

    private fun take(id: Long): Entry? {
        var taken: Entry? = null
        update { current ->
            taken = current.firstOrNull { it.action.id == id }
            current.filterNot { it.action.id == id }
        }
        return taken
    }

    private fun update(transform: (List<Entry>) -> List<Entry>) {
        entries.update(transform)
        actions.value = entries.value.map { it.action }
    }
}

fun <T> List<T>.withoutPending(pending: List<PendingMailAction>, key: (T) -> Long): List<T> {
    if (pending.isEmpty()) return this
    val hidden = pending.flatMapTo(HashSet()) { it.messageKeys }
    return filterNot { key(it) in hidden }
}

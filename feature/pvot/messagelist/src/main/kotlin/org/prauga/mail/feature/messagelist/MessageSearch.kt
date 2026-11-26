// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

internal fun List<MessageRow>.matching(query: String): List<MessageRow> {
    val needle = query.trim()
    if (needle.isEmpty()) return this
    return filter { row ->
        row.sender.contains(needle, ignoreCase = true) ||
            row.subject.contains(needle, ignoreCase = true) ||
            row.preview.contains(needle, ignoreCase = true)
    }
}

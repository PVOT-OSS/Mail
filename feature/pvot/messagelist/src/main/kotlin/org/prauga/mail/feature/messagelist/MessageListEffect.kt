// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

sealed interface MessageListEffect {
    data class ShowArchiveUndo(val key: Long) : MessageListEffect
}

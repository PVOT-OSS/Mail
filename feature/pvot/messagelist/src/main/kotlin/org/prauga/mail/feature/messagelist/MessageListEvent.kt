// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

sealed interface MessageListEvent {
    data class Archive(val row: MessageRow) : MessageListEvent
    data class ToggleRead(val row: MessageRow) : MessageListEvent
}

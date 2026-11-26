// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import app.k9mail.legacy.message.controller.MessageReference

data class MessageListUiState(
    val isLoading: Boolean = true,
    val rows: List<MessageRow> = emptyList(),
    val filter: FilterState = FilterState(),
)

data class MessageRow(
    val key: Long,
    val sender: String,
    val senderKey: String,
    val subject: String,
    val preview: String,
    val date: String,
    val isUnread: Boolean,
    val isStarred: Boolean,
    val hasAttachments: Boolean,
    val threadCount: Int,
    val reference: MessageReference,
)

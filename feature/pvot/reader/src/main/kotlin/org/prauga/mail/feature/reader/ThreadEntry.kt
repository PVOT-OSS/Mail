// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import app.k9mail.legacy.message.controller.MessageReference

data class ThreadEntry(
    val key: Long,
    val reference: MessageReference,
    val senderName: String,
    val senderAddress: String,
    val sentAt: Long,
    val preview: String,
    val isUnread: Boolean,
)

internal fun List<ThreadEntry>.initiallyExpanded(opened: MessageReference): Set<Long> =
    setOfNotNull(firstOrNull { it.reference == opened }?.key, lastOrNull()?.key)

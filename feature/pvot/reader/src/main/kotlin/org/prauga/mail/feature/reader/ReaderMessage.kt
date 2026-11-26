// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import com.fsck.k9.mail.Address
import com.fsck.k9.mailstore.AttachmentResolver

data class ReaderMessage(
    val databaseId: Long,
    val subject: String,
    val senderName: String,
    val senderAddress: String,
    val recipients: String,
    val sentAt: Long,
    val html: String,
    val attachmentResolver: AttachmentResolver?,
    val attachmentNames: List<String>,
    val isUnread: Boolean,
    val isStarred: Boolean,
)

internal fun senderNameOf(from: List<Address>): String =
    from.firstOrNull()?.let { it.personal?.takeIf(String::isNotBlank) ?: it.address }.orEmpty()

internal fun recipientsOf(addresses: List<Address>): String =
    addresses.joinToString { it.personal?.takeIf(String::isNotBlank) ?: it.address.orEmpty() }

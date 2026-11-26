// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import android.net.Uri
import com.fsck.k9.mail.Address
import com.fsck.k9.mail.Part
import com.fsck.k9.mailstore.AttachmentResolver
import kotlinx.collections.immutable.ImmutableList

data class ReaderMessage(
    val databaseId: Long,
    val subject: String,
    val senderName: String,
    val senderAddress: String,
    val recipients: String,
    val sentAt: Long,
    val html: String,
    val attachmentResolver: AttachmentResolver?,
    val attachments: ImmutableList<ReaderAttachment>,
    val isUnread: Boolean,
    val isStarred: Boolean,
    val hasRemoteImages: Boolean,
    val loadRemoteImages: Boolean,
    val suspiciousLinks: Map<String, SuspiciousLink>,
)

data class ReaderAttachment(
    val name: String,
    val size: Long,
    val mimeType: String,
    val internalUri: Uri,
    val isDownloaded: Boolean,
    val part: Part?,
)

internal fun senderNameOf(from: List<Address>): String =
    from.firstOrNull()?.let { it.personal?.takeIf(String::isNotBlank) ?: it.address }.orEmpty()

internal fun recipientsOf(addresses: List<Address>): String =
    addresses.joinToString { it.personal?.takeIf(String::isNotBlank) ?: it.address.orEmpty() }

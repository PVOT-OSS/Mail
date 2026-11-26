// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

data class ComposerUiState(
    val fromAddresses: List<String> = emptyList(),
    val fromIndex: Int = 0,
    val to: String = "",
    val cc: String = "",
    val bcc: String = "",
    val subject: String = "",
    val body: String = "",
    val signature: String? = null,
    val quotedText: String? = null,
    val showCcBcc: Boolean = false,
    val isSending: Boolean = false,
) {
    val from: String
        get() = fromAddresses.getOrElse(fromIndex) { "" }

    val canSend: Boolean
        get() = !isSending && (recipientsOf(to) + recipientsOf(cc) + recipientsOf(bcc)).isNotEmpty()
}

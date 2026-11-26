// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.navigation

import androidx.navigation3.runtime.NavKey
import app.k9mail.legacy.message.controller.MessageReference
import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import net.thunderbird.feature.account.AccountId
import net.thunderbird.feature.account.AccountIdFactory
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import net.thunderbird.feature.search.legacy.serialization.LocalMessageSearchSerializer
import org.prauga.mail.feature.composer.ComposeMode
import org.prauga.mail.feature.composer.ComposeSource

@Serializable
data object MailboxesRoute : NavKey

@Serializable
data class MessageListRoute(
    val title: String,
    val subtitle: String?,
    private val encodedSearch: String,
) : NavKey {
    val search: LocalMessageSearch
        get() = LocalMessageSearchSerializer.deserialize(Base64.UrlSafe.decode(encodedSearch))

    companion object {
        fun of(title: String, search: LocalMessageSearch, subtitle: String? = null) =
            MessageListRoute(title, subtitle, Base64.UrlSafe.encode(LocalMessageSearchSerializer.serialize(search)))
    }
}

@Serializable
data class MessageSearchRoute(val scopeTitle: String, private val encodedSearch: String) : NavKey {
    val search: LocalMessageSearch
        get() = LocalMessageSearchSerializer.deserialize(Base64.UrlSafe.decode(encodedSearch))

    companion object {
        fun of(scopeTitle: String, search: LocalMessageSearch) =
            MessageSearchRoute(scopeTitle, Base64.UrlSafe.encode(LocalMessageSearchSerializer.serialize(search)))
    }
}

@Serializable
data class ComposeRoute(
    private val accountId: String? = null,
    private val sourceIdentity: String? = null,
    private val mode: ComposeMode? = null,
) : NavKey {
    val account: AccountId?
        get() = accountId?.let { AccountIdFactory.of(it) }

    val source: ComposeSource?
        get() {
            val reference = sourceIdentity?.let { MessageReference.parse(it) }
            return if (reference != null && mode != null) ComposeSource(reference, mode) else null
        }

    companion object {
        fun of(account: AccountId?) = ComposeRoute(account?.toString())

        fun of(source: ComposeSource) = ComposeRoute(
            accountId = source.reference.accountId.toString(),
            sourceIdentity = source.reference.toIdentityString(),
            mode = source.mode,
        )
    }
}

@Serializable
data class MessageRoute(private val identity: String) : NavKey {
    val reference: MessageReference
        get() = requireNotNull(MessageReference.parse(identity)) { "Invalid message reference" }

    companion object {
        fun of(reference: MessageReference) = MessageRoute(reference.toIdentityString())
    }
}

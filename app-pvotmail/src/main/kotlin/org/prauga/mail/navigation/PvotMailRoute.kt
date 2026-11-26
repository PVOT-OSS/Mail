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
data class ComposeRoute(private val accountId: String? = null) : NavKey {
    val account: AccountId?
        get() = accountId?.let { AccountIdFactory.of(it) }

    companion object {
        fun of(account: AccountId?) = ComposeRoute(account?.toString())
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

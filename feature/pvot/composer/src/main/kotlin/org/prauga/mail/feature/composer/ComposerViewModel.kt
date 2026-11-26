// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.thunderbird.components.core.logging.Logger
import net.thunderbird.core.android.account.LegacyAccount
import net.thunderbird.core.android.account.LegacyAccountManager
import net.thunderbird.feature.account.AccountId

private const val TAG = "ComposerViewModel"

class ComposerViewModel(
    private val accountId: AccountId?,
    private val source: ComposeSource?,
    private val accountManager: LegacyAccountManager,
    private val messageSender: MessageSender,
    private val replyDraftLoader: ReplyDraftLoader,
    private val logger: Logger,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ComposerUiState())
    val state: StateFlow<ComposerUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<ComposerEffect>(Channel.BUFFERED)
    val effects: Flow<ComposerEffect> = effectChannel.receiveAsFlow()

    private var account: LegacyAccount? = null
    private var replyHeaders: ReplyHeaders? = null

    init {
        viewModelScope.launch {
            val found = withContext(ioDispatcher) { findAccount() } ?: return@launch
            account = found
            mutableState.update { state ->
                state.copy(fromAddresses = found.identities.map { it.email.orEmpty() }).withSignature(found, 0)
            }
            source?.let { prefill(found, it) }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun prefill(account: LegacyAccount, source: ComposeSource) {
        val draft = withContext(ioDispatcher) {
            try {
                replyDraftLoader.load(account, source)
            } catch (e: Exception) {
                logger.error(tag = TAG, throwable = e) { "Couldn't load the message to reply to" }
                null
            }
        } ?: return
        replyHeaders = ReplyHeaders(
            source = source,
            quotedText = draft.quotedText,
            inReplyTo = draft.inReplyTo,
            references = draft.references,
            signatureBeforeQuotedText = account.isSignatureBeforeQuotedText,
        )
        mutableState.update { state ->
            state.copy(
                to = draft.to.joinToString(", "),
                cc = draft.cc.joinToString(", "),
                subject = draft.subject,
                quotedText = draft.quotedText,
                showCcBcc = draft.cc.isNotEmpty(),
            ).withSignature(account, draft.identityIndex)
        }
    }

    fun onEvent(event: ComposerEvent) {
        when (event) {
            is ComposerEvent.ChangeTo -> mutableState.update { it.copy(to = event.text) }

            is ComposerEvent.ChangeCc -> mutableState.update { it.copy(cc = event.text) }

            is ComposerEvent.ChangeBcc -> mutableState.update { it.copy(bcc = event.text) }

            is ComposerEvent.ChangeSubject -> mutableState.update { it.copy(subject = event.text) }

            is ComposerEvent.ChangeBody -> mutableState.update { it.copy(body = event.text) }

            is ComposerEvent.SelectFrom -> account?.let { found ->
                mutableState.update { it.withSignature(found, event.index) }
            }

            ComposerEvent.ShowCcBcc -> mutableState.update { it.copy(showCcBcc = true) }

            ComposerEvent.Send -> send()
        }
    }

    private fun send() {
        val found = account ?: return
        val current = mutableState.value
        if (!current.canSend) return
        mutableState.update { it.copy(isSending = true) }
        viewModelScope.launch {
            val result = messageSender.send(
                OutgoingMessage(
                    accountId = found.id,
                    identity = found.getIdentity(current.fromIndex),
                    to = recipientsOf(current.to),
                    cc = recipientsOf(current.cc),
                    bcc = recipientsOf(current.bcc),
                    subject = current.subject,
                    body = current.body,
                    reply = replyHeaders,
                ),
            )
            mutableState.update { it.copy(isSending = false) }
            effectChannel.send(
                result.fold(
                    onSuccess = { ComposerEffect.Sent },
                    onFailure = { ComposerEffect.Failed(it.message) },
                ),
            )
        }
    }

    private fun findAccount(): LegacyAccount? {
        val accounts = accountManager.findAll().filter { it.isFinishedSetup }
        return accounts.firstOrNull { it.id == accountId } ?: accounts.firstOrNull()
    }

    private fun ComposerUiState.withSignature(account: LegacyAccount, index: Int): ComposerUiState {
        val identity = account.identities.getOrNull(index)
        return copy(
            fromIndex = index,
            signature = identity?.signature?.takeIf { identity.signatureUse && it.isNotBlank() },
        )
    }
}

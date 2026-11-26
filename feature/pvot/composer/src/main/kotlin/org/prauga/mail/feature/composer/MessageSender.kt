// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import android.app.PendingIntent
import com.fsck.k9.controller.MessagingController
import com.fsck.k9.mail.Address
import com.fsck.k9.mail.internet.MimeMessage
import com.fsck.k9.message.MessageBuilder
import com.fsck.k9.message.QuotedTextMode
import com.fsck.k9.message.SimpleMessageBuilder
import com.fsck.k9.message.SimpleMessageFormat
import java.util.Date
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.thunderbird.core.android.account.Identity
import net.thunderbird.core.android.account.QuoteStyle
import net.thunderbird.core.common.exception.MessagingException
import net.thunderbird.core.preference.GeneralSettingsManager
import net.thunderbird.feature.account.AccountId

data class OutgoingMessage(
    val accountId: AccountId,
    val identity: Identity,
    val to: List<Address>,
    val cc: List<Address>,
    val bcc: List<Address>,
    val subject: String,
    val body: String,
    val reply: ReplyHeaders? = null,
)

data class ReplyHeaders(
    val source: ComposeSource,
    val quotedText: String,
    val inReplyTo: String?,
    val references: String?,
    val signatureBeforeQuotedText: Boolean,
)

class MessageSender(
    private val messagingController: MessagingController,
    private val generalSettingsManager: GeneralSettingsManager,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun send(message: OutgoingMessage): Result<Unit> {
        val built = withContext(Dispatchers.Main) { build(message) }
        return built.mapCatching { mime ->
            withContext(ioDispatcher) {
                messagingController.sendMessage(message.accountId, mime, null, null)
                message.reply?.source?.let { markRelated(it) }
            }
        }
    }

    private fun markRelated(source: ComposeSource) {
        val reference = source.reference
        messagingController.setFlag(
            reference.accountId,
            reference.folderId,
            reference.uid,
            source.mode.relatedFlag,
            true,
        )
    }

    private suspend fun build(message: OutgoingMessage): Result<MimeMessage> = suspendCancellableCoroutine { cont ->
        val builder = SimpleMessageBuilder.newInstance()
            .setSubject(message.subject.replace('\n', ' '))
            .setSentDate(Date())
            .setHideTimeZone(generalSettingsManager.getConfig().privacy.isHideTimeZone)
            .setTo(message.to)
            .setCc(message.cc)
            .setBcc(message.bcc)
            .setIdentity(message.identity)
            .setMessageFormat(SimpleMessageFormat.TEXT)
            .setText(message.body.toCrLf())
            .setAttachments(emptyList())
            .setSignature(message.identity.signature?.toCrLf())
            .setQuoteStyle(QuoteStyle.PREFIX)
            .setQuotedTextMode(if (message.reply == null) QuotedTextMode.NONE else QuotedTextMode.SHOW)
            .setQuotedText(message.reply?.quotedText.orEmpty())
            .setInReplyTo(message.reply?.inReplyTo)
            .setReferences(message.reply?.references)
            .setSignatureBeforeQuotedText(message.reply?.signatureBeforeQuotedText ?: false)
            .setDraft(false)
        builder.buildAsync(
            object : MessageBuilder.Callback {
                override fun onMessageBuildSuccess(message: MimeMessage, isDraft: Boolean) {
                    cont.resume(Result.success(message))
                }

                override fun onMessageBuildCancel() {
                    cont.resume(Result.failure(MessagingException("Cancelled")))
                }

                override fun onMessageBuildException(exception: MessagingException) {
                    cont.resume(Result.failure(exception))
                }

                override fun onMessageBuildReturnPendingIntent(pendingIntent: PendingIntent, requestCode: Int) {
                    cont.resume(Result.failure(MessagingException("Needs user interaction")))
                }
            },
        )
    }

    private fun String.toCrLf(): String = replace("\r\n", "\n").replace("\n", "\r\n")
}

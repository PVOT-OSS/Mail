// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.controller.MessagingController
import com.fsck.k9.helper.IdentityHelper
import com.fsck.k9.helper.ReplyToParser
import com.fsck.k9.mail.Address
import com.fsck.k9.message.SimpleMessageFormat
import com.fsck.k9.message.extractors.BodyTextExtractor
import com.fsck.k9.message.quote.TextQuoteCreator
import com.fsck.k9.message.signature.TextSignatureRemover
import net.thunderbird.core.android.account.LegacyAccount
import net.thunderbird.core.android.account.QuoteStyle
import net.thunderbird.core.common.mail.Flag

private const val DEFAULT_QUOTE_PREFIX = ">"
private val replyPrefix = Regex("^(re:|aw[:\\s])\\s*", RegexOption.IGNORE_CASE)
private val forwardPrefix = Regex("^fwd?:\\s*", RegexOption.IGNORE_CASE)

enum class ComposeMode(internal val relatedFlag: Flag) {
    REPLY(Flag.ANSWERED),
    REPLY_ALL(Flag.ANSWERED),
    FORWARD(Flag.FORWARDED),
}

data class ComposeSource(val reference: MessageReference, val mode: ComposeMode)

internal data class ReplyDraft(
    val identityIndex: Int,
    val to: List<Address>,
    val cc: List<Address>,
    val subject: String,
    val quotedText: String,
    val inReplyTo: String?,
    val references: String?,
)

internal fun replySubject(subject: String?): String = "Re: " + subject.orEmpty().replaceFirst(replyPrefix, "")

internal fun forwardSubject(subject: String?): String = "Fwd: " + subject.orEmpty().replaceFirst(forwardPrefix, "")

internal fun referencesFor(messageId: String, references: List<String>, mode: ComposeMode): String =
    if (mode == ComposeMode.FORWARD || references.isEmpty()) {
        messageId
    } else {
        (references + messageId).joinToString(" ")
    }

class ReplyDraftLoader(
    private val messagingController: MessagingController,
    private val textQuoteCreator: TextQuoteCreator,
    private val replyToParser: ReplyToParser = ReplyToParser(),
) {
    internal fun load(account: LegacyAccount, source: ComposeSource): ReplyDraft {
        val reference = source.reference
        val message = messagingController.loadMessage(reference.accountId, reference.folderId, reference.uid)
        val recipients = when (source.mode) {
            ComposeMode.REPLY -> replyToParser.getRecipientsToReplyTo(message, account)
            ComposeMode.REPLY_ALL -> replyToParser.getRecipientsToReplyAllTo(message, account)
            ComposeMode.FORWARD -> null
        }
        val isForward = source.mode == ComposeMode.FORWARD
        val body = BodyTextExtractor.getBodyTextFromMessage(message, SimpleMessageFormat.TEXT).let { text ->
            if (!isForward && account.isStripSignature) TextSignatureRemover.stripSignature(text) else text
        }
        val messageId = message.messageId?.takeIf { it.isNotBlank() }
        return ReplyDraft(
            identityIndex = account.identities.indexOf(IdentityHelper.getRecipientIdentityFromMessage(account, message))
                .coerceAtLeast(0),
            to = recipients?.to?.toList().orEmpty(),
            cc = recipients?.cc?.toList().orEmpty(),
            subject = if (isForward) forwardSubject(message.subject) else replySubject(message.subject),
            quotedText = textQuoteCreator.quoteOriginalTextMessage(
                originalMessage = message,
                messageBody = body,
                quoteStyle = if (isForward) QuoteStyle.HEADER else account.quoteStyle,
                prefix = account.quotePrefix ?: DEFAULT_QUOTE_PREFIX,
            ),
            inReplyTo = messageId,
            references = messageId?.let { referencesFor(it, message.references.orEmpty().toList(), source.mode) },
        )
    }
}

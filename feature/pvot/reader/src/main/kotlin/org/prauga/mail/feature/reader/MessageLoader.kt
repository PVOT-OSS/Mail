// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import app.k9mail.legacy.message.controller.MessageReference
import app.k9mail.legacy.message.controller.SimpleMessagingListener
import com.fsck.k9.controller.MessagingController
import com.fsck.k9.mail.Message.RecipientType
import com.fsck.k9.mailstore.LocalMessage
import com.fsck.k9.mailstore.MessageViewInfoExtractorFactory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.thunderbird.core.common.mail.Flag
import net.thunderbird.feature.account.AccountId
import net.thunderbird.feature.mail.message.reader.api.html.MessageReaderHtmlSettingsProvider

class MessageLoader(
    private val messagingController: MessagingController,
    private val extractorFactory: MessageViewInfoExtractorFactory,
    private val htmlSettingsProvider: MessageReaderHtmlSettingsProvider,
    private val ioDispatcher: CoroutineDispatcher,
) {
    @Suppress("TooGenericExceptionCaught")
    suspend fun load(reference: MessageReference): Result<ReaderMessage> = withContext(ioDispatcher) {
        try {
            var message = loadLocal(reference)
            if (!message.isSet(Flag.X_DOWNLOADED_FULL) && !message.isSet(Flag.X_DOWNLOADED_PARTIAL)) {
                downloadBody(reference).getOrThrow()
                message = loadLocal(reference)
            }
            val viewInfo = extractorFactory.create(htmlSettingsProvider.create())
                .extractMessageForView(message, null, false)
            Result.success(
                ReaderMessage(
                    databaseId = message.databaseId,
                    subject = viewInfo.subject ?: message.subject.orEmpty(),
                    senderName = senderNameOf(message.from.orEmpty().toList()),
                    senderAddress = message.from?.firstOrNull()?.address.orEmpty(),
                    recipients = recipientsOf(
                        message.getRecipients(RecipientType.TO).orEmpty().toList() +
                            message.getRecipients(RecipientType.CC).orEmpty().toList(),
                    ),
                    sentAt = message.sentDate?.time ?: message.internalDate?.time ?: 0L,
                    html = viewInfo.text.orEmpty(),
                    attachmentResolver = viewInfo.attachmentResolver,
                    attachmentNames = viewInfo.attachments.orEmpty().mapNotNull { it.displayName },
                    isUnread = !message.isSet(Flag.SEEN),
                    isStarred = message.isSet(Flag.FLAGGED),
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadLocal(reference: MessageReference): LocalMessage =
        messagingController.loadMessage(reference.accountId, reference.folderId, reference.uid)

    private suspend fun downloadBody(reference: MessageReference): Result<Unit> = suspendCancellableCoroutine { cont ->
        messagingController.loadMessageRemote(
            reference.accountId,
            reference.folderId,
            reference.uid,
            object : SimpleMessagingListener() {
                override fun loadMessageRemoteFinished(accountId: AccountId, folderId: Long, uid: String) {
                    if (cont.isActive) cont.resume(Result.success(Unit))
                }

                override fun loadMessageRemoteFailed(accountId: AccountId, folderId: Long, uid: String, t: Throwable) {
                    if (cont.isActive) cont.resume(Result.failure(t))
                }
            },
        )
    }
}

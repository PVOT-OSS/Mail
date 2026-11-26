// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import app.k9mail.legacy.message.controller.MessageReference
import app.k9mail.legacy.message.controller.SimpleMessagingListener
import com.fsck.k9.controller.MessagingController
import com.fsck.k9.mail.Message
import com.fsck.k9.mail.Part
import com.fsck.k9.provider.AttachmentTempFileProvider
import java.io.IOException
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.thunderbird.components.core.logging.Logger
import net.thunderbird.feature.account.AccountId

private const val TAG = "AttachmentOpener"

class AttachmentOpener(
    private val context: Context,
    private val messagingController: MessagingController,
    private val logger: Logger,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun open(reference: MessageReference, attachment: ReaderAttachment): Boolean {
        val available = attachment.isDownloaded || download(reference, attachment)
        val uri = if (available) temporaryUri(attachment) else null
        return uri != null && startViewer(uri, attachment)
    }

    private suspend fun temporaryUri(attachment: ReaderAttachment): Uri? = try {
        withContext(ioDispatcher) {
            AttachmentTempFileProvider.createTempUriForContentUri(context, attachment.internalUri, attachment.name)
        }
    } catch (e: IOException) {
        logger.error(tag = TAG, throwable = e) { "Couldn't copy an attachment for viewing" }
        null
    }

    private fun startViewer(uri: Uri, attachment: ReaderAttachment): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, attachment.mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(Intent.createChooser(intent, attachment.name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            logger.warn(tag = TAG, throwable = e) { "No app can open this attachment type" }
            false
        }
    }

    private suspend fun download(reference: MessageReference, attachment: ReaderAttachment): Boolean {
        val part = attachment.part ?: return false
        val message = withContext(ioDispatcher) {
            messagingController.loadMessage(reference.accountId, reference.folderId, reference.uid)
        }
        return suspendCancellableCoroutine { cont ->
            messagingController.loadAttachment(
                reference.accountId,
                message,
                part,
                object : SimpleMessagingListener() {
                    override fun loadAttachmentFinished(accountId: AccountId?, message: Message?, part: Part?) {
                        if (cont.isActive) cont.resume(true)
                    }

                    override fun loadAttachmentFailed(
                        accountId: AccountId?,
                        message: Message?,
                        part: Part?,
                        reason: String?,
                    ) {
                        if (cont.isActive) cont.resume(false)
                    }
                },
            )
        }
    }
}

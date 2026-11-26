package app.k9mail.backend.demo

import com.fsck.k9.backend.api.BackendFolder.MoreMessages
import com.fsck.k9.backend.api.BackendStorage
import com.fsck.k9.backend.api.SyncListener
import com.fsck.k9.mail.MessageDownloadState

internal class CommandSync(
    private val backendStorage: BackendStorage,
    private val demoStore: DemoStore,
) {

    suspend fun sync(folderServerId: String, listener: SyncListener) {
        listener.syncStarted(folderServerId)

        val folder = demoStore.getFolder(folderServerId)
        if (folder == null) {
            listener.syncFailed(folderServerId, "Folder $folderServerId doesn't exist", null)
            return
        }

        val backendFolder = backendStorage.getFolder(folderServerId)

        val delivered = backendFolder.getFolderExtraString(DELIVERED_KEY)?.split(DELIVERED_SEPARATOR)?.toSet()
            ?: backendFolder.getMessageServerIds()
        val newMessageServerIds = folder.messageServerIds.filterNot { it in delivered }
        for (messageServerId in newMessageServerIds) {
            val message = demoStore.getMessage(folderServerId, messageServerId)
            backendFolder.saveMessage(message, MessageDownloadState.FULL)
            listener.syncNewMessage(folderServerId, messageServerId, isOldMessage = false)
        }
        backendFolder.setFolderExtraString(
            DELIVERED_KEY,
            (delivered + newMessageServerIds).joinToString(DELIVERED_SEPARATOR),
        )

        backendFolder.setMoreMessages(MoreMessages.FALSE)
        backendFolder.setLastChecked(System.currentTimeMillis())

        listener.syncFinished(folderServerId)
    }

    private companion object {
        const val DELIVERED_KEY = "demoDeliveredMessageServerIds"
        const val DELIVERED_SEPARATOR = ","
    }
}

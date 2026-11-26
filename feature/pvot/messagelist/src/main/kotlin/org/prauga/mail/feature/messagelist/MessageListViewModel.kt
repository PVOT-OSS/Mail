// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.k9mail.legacy.mailstore.MessageListChangedListener
import app.k9mail.legacy.mailstore.MessageListRepository
import com.fsck.k9.controller.MessagingControllerWrapper
import com.fsck.k9.ui.messagelist.MessageListConfig
import com.fsck.k9.ui.messagelist.MessageListItem
import com.fsck.k9.ui.messagelist.MessageListLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.thunderbird.core.android.account.SortType
import net.thunderbird.core.common.mail.Flag
import net.thunderbird.feature.search.legacy.LocalMessageSearch

private const val STOP_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
class MessageListViewModel(
    search: LocalMessageSearch,
    private val messageListLoader: MessageListLoader,
    private val messageListRepository: MessageListRepository,
    private val messagingController: MessagingControllerWrapper,
    private val dateFormatter: MessageDateFormatter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val config = MessageListConfig(
        search = search,
        showingThreadedList = false,
        sortType = SortType.SORT_DATE,
        sortAscending = false,
        sortDateAscending = false,
        activeMessage = null,
        sortOverrides = emptyMap(),
    )

    val state: StateFlow<MessageListUiState> = callbackFlow {
        val listener = MessageListChangedListener { trySend(Unit) }
        messageListRepository.addListener(listener)
        send(Unit)
        awaitClose { messageListRepository.removeListener(listener) }
    }
        .mapLatest {
            MessageListUiState(
                isLoading = false,
                rows = messageListLoader.getMessageList(config).messageListItems.map { it.toRow() },
            )
        }
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MessageListUiState())

    fun onEvent(event: MessageListEvent) {
        viewModelScope.launch(ioDispatcher) {
            when (event) {
                is MessageListEvent.Archive -> messagingController.archiveMessages(listOf(event.row.reference))
                is MessageListEvent.ToggleRead -> setFlag(event.row, Flag.SEEN, newState = event.row.isUnread)
            }
        }
    }

    private fun setFlag(row: MessageRow, flag: Flag, newState: Boolean) {
        messagingController.setFlag(row.reference.accountId, listOf(row.key), flag, newState)
    }

    private fun MessageListItem.toRow() = MessageRow(
        key = databaseId,
        sender = displayName.toString(),
        subject = subject.orEmpty(),
        preview = previewText,
        date = dateFormatter.format(messageDate),
        isUnread = !isRead,
        isStarred = isStarred,
        hasAttachments = hasAttachments,
        threadCount = threadCount,
        reference = messageReference,
    )
}

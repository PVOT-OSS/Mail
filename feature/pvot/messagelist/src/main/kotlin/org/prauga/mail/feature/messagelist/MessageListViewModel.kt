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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val pendingArchives = PendingArchives()
    private val filterState = MutableStateFlow(FilterState())
    private val query = MutableStateFlow("")
    private val effectChannel = Channel<MessageListEffect>(Channel.BUFFERED)
    val effects: Flow<MessageListEffect> = effectChannel.receiveAsFlow()

    private val loadedRows = callbackFlow {
        val listener = MessageListChangedListener { trySend(Unit) }
        messageListRepository.addListener(listener)
        send(Unit)
        awaitClose { messageListRepository.removeListener(listener) }
    }
        .mapLatest { messageListLoader.getMessageList(config).messageListItems.map { it.toRow() } }
        .flowOn(ioDispatcher)

    val state: StateFlow<MessageListUiState> = combine(loadedRows, pendingArchives.keys, filterState, query) {
            rows,
            pending,
            filter,
            query,
        ->
        MessageListUiState(
            isLoading = false,
            rows = rows.without(pending).filteredBy(filter).matching(query),
            filter = filter,
            query = query,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MessageListUiState())

    fun onEvent(event: MessageListEvent) {
        when (event) {
            is MessageListEvent.Archive -> {
                pendingArchives.add(event.row)
                effectChannel.trySend(MessageListEffect.ShowArchiveUndo(event.row.key))
            }

            is MessageListEvent.UndoArchive -> pendingArchives.undo(event.key)

            is MessageListEvent.Search -> query.value = event.query

            MessageListEvent.ToggleFilter -> filterState.update { it.copy(enabled = !it.enabled) }

            is MessageListEvent.ToggleInclude -> filterState.update { state ->
                val include = if (event.filter in
                    state.include
                ) {
                    state.include - event.filter
                } else {
                    state.include + event.filter
                }
                state.copy(include = include)
            }

            is MessageListEvent.SetOnlyWithAttachments -> filterState.update {
                it.copy(onlyWithAttachments = event.value)
            }

            is MessageListEvent.CommitArchive -> pendingArchives.take(event.key)?.let { archive(listOf(it)) }

            is MessageListEvent.ToggleRead -> viewModelScope.launch(ioDispatcher) {
                setFlag(event.row, Flag.SEEN, newState = event.row.isUnread)
            }
        }
    }

    override fun onCleared() {
        archive(pendingArchives.takeAll())
    }

    private fun archive(rows: List<MessageRow>) {
        if (rows.isNotEmpty()) messagingController.archiveMessages(rows.map { it.reference })
    }

    private fun setFlag(row: MessageRow, flag: Flag, newState: Boolean) {
        messagingController.setFlag(row.reference.accountId, listOf(row.key), flag, newState)
    }

    private fun MessageListItem.toRow() = MessageRow(
        key = databaseId,
        sender = displayName.toString(),
        senderKey = displayAddress?.address ?: displayName.toString(),
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

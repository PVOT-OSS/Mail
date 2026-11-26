// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.messagelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.k9mail.legacy.mailstore.MessageListChangedListener
import app.k9mail.legacy.mailstore.MessageListRepository
import app.k9mail.legacy.message.controller.MessagingControllerMailChecker
import com.fsck.k9.controller.MessagingControllerWrapper
import com.fsck.k9.ui.messagelist.MessageListConfig
import com.fsck.k9.ui.messagelist.MessageListItem
import com.fsck.k9.ui.messagelist.MessageListLoader
import java.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.thunderbird.core.android.account.LegacyAccountManager
import net.thunderbird.core.android.account.SortType
import net.thunderbird.core.common.mail.Flag
import net.thunderbird.feature.search.legacy.LocalMessageSearch
import org.prauga.mail.core.mailactions.MailActionKind
import org.prauga.mail.core.mailactions.PendingMailAction
import org.prauga.mail.core.mailactions.PendingMailActions
import org.prauga.mail.core.mailactions.withoutPending
import org.prauga.mail.core.syncstatus.RefreshListener
import org.prauga.mail.core.syncstatus.RefreshState

private const val STOP_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongParameterList")
class MessageListViewModel(
    private val search: LocalMessageSearch,
    private val messageListLoader: MessageListLoader,
    private val messageListRepository: MessageListRepository,
    private val messagingController: MessagingControllerWrapper,
    private val dateFormatter: MessageDateFormatter,
    accountManager: LegacyAccountManager,
    private val mailChecker: MessagingControllerMailChecker,
    private val clock: Clock,
    private val pendingActions: PendingMailActions,
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

    private val filterState = MutableStateFlow(FilterState())
    private val query = MutableStateFlow("")
    private val refresh = MutableStateFlow(RefreshState())

    private val accountsSyncTime = accountManager.observeAll().map { accounts ->
        accounts.filter { search.accountIds.isEmpty() || it.id in search.accountIds }.maxOfOrNull { it.lastSyncTime }
            ?: 0L
    }
    val pending: StateFlow<List<PendingMailAction>> = pendingActions.pending

    private val loadedRows = callbackFlow {
        val listener = MessageListChangedListener { trySend(Unit) }
        messageListRepository.addListener(listener)
        send(Unit)
        awaitClose { messageListRepository.removeListener(listener) }
    }
        .mapLatest { messageListLoader.getMessageList(config).messageListItems.map { it.toRow() } }
        .flowOn(ioDispatcher)

    private val listState =
        combine(loadedRows, pendingActions.pending, filterState, query) { rows, pending, filter, query ->
            MessageListUiState(
                isLoading = false,
                rows = rows.withoutPending(pending) { it.key }.filteredBy(filter).matching(query),
                filter = filter,
                query = query,
            )
        }

    val state: StateFlow<MessageListUiState> = combine(listState, refresh, accountsSyncTime) { list, refresh, synced ->
        list.copy(isRefreshing = refresh.running, lastUpdated = maxOf(synced, refresh.lastSuccess))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MessageListUiState())

    fun onEvent(event: MessageListEvent) {
        when (event) {
            is MessageListEvent.Archive -> pendingActions.add(MailActionKind.ARCHIVE, setOf(event.row.key)) {
                messagingController.archiveMessages(listOf(event.row.reference))
            }

            is MessageListEvent.UndoAction -> pendingActions.undo(event.id)

            is MessageListEvent.CommitAction -> pendingActions.commit(event.id)

            is MessageListEvent.Search -> query.value = event.query

            MessageListEvent.Refresh -> startRefresh()

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

            is MessageListEvent.ToggleRead -> viewModelScope.launch(ioDispatcher) {
                setFlag(event.row, Flag.SEEN, newState = event.row.isUnread)
            }
        }
    }

    private fun startRefresh() {
        if (refresh.value.running) return
        refresh.update { it.copy(running = true) }
        val accountId = search.accountIds.singleOrNull()
        val folderId = search.folderIds.singleOrNull()
        val singleFolder = accountId != null && folderId != null
        val listener = RefreshListener(singleFolder, ::finishRefresh)
        if (singleFolder) {
            messagingController.synchronizeMailbox(
                requireNotNull(accountId),
                requireNotNull(folderId),
                notify = false,
                listener = listener,
            )
        } else {
            mailChecker.checkMail(
                accountId = accountId,
                ignoreLastCheckedTime = true,
                useManualWakeLock = true,
                notify = false,
                listener = listener,
            )
        }
    }

    private fun finishRefresh(success: Boolean) {
        refresh.update { state ->
            RefreshState(running = false, lastSuccess = if (success) clock.millis() else state.lastSuccess)
        }
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

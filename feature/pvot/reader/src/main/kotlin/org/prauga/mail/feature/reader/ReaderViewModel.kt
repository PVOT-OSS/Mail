// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.controller.MessagingControllerWrapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.thunderbird.core.common.mail.Flag

sealed interface ReaderEffect {
    data object AttachmentFailed : ReaderEffect
    data object Close : ReaderEffect
}

data class ReaderUiState(
    val isLoading: Boolean = true,
    val failed: Boolean = false,
    val entries: ImmutableList<ThreadEntry> = persistentListOf(),
    val expanded: ImmutableSet<Long> = persistentSetOf(),
    val messages: ImmutableMap<Long, ReaderMessage> = persistentMapOf(),
    val remoteImagesShown: ImmutableSet<Long> = persistentSetOf(),
    val isFlagged: Boolean = false,
) {
    val subject: String?
        get() = messages.values.firstOrNull()?.subject
}

class ReaderViewModel(
    private val reference: MessageReference,
    private val loader: MessageLoader,
    private val messagingController: MessagingControllerWrapper,
    private val attachmentOpener: AttachmentOpener,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<ReaderEffect>(Channel.BUFFERED)
    val effects: Flow<ReaderEffect> = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            loader.thread(reference).fold(
                onSuccess = { entries ->
                    val expanded = entries.initiallyExpanded(reference)
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            entries = entries.toImmutableList(),
                            expanded = expanded.toImmutableSet(),
                        )
                    }
                    entries.filter { it.key in expanded }.forEach { loadBody(it) }
                },
                onFailure = { mutableState.update { it.copy(isLoading = false, failed = true) } },
            )
        }
    }

    fun toggle(entry: ThreadEntry) {
        val isExpanded = entry.key in mutableState.value.expanded
        mutableState.update { state ->
            val expanded = if (isExpanded) state.expanded - entry.key else state.expanded + entry.key
            state.copy(expanded = expanded.toImmutableSet())
        }
        if (!isExpanded) loadBody(entry)
    }

    fun loadRemoteImages(entry: ThreadEntry) {
        mutableState.update { it.copy(remoteImagesShown = (it.remoteImagesShown + entry.key).toImmutableSet()) }
    }

    fun openAttachment(entry: ThreadEntry, attachment: ReaderAttachment) {
        viewModelScope.launch {
            if (!attachmentOpener.open(entry.reference, attachment)) effectChannel.send(ReaderEffect.AttachmentFailed)
        }
    }

    fun archive() = closeAfter { messagingController.archiveMessages(it.map(ThreadEntry::reference)) }

    fun delete() = closeAfter { messagingController.deleteMessages(it.map(ThreadEntry::reference)) }

    fun markUnread() = closeAfter { entries ->
        val newest = entries.last()
        messagingController.setFlag(newest.reference.accountId, listOf(newest.key), Flag.SEEN, false)
    }

    fun toggleFlag() {
        val newest = mutableState.value.entries.lastOrNull() ?: return
        val flagged = !mutableState.value.isFlagged
        messagingController.setFlag(newest.reference.accountId, listOf(newest.key), Flag.FLAGGED, flagged)
        mutableState.update { it.copy(isFlagged = flagged) }
    }

    private fun closeAfter(action: (List<ThreadEntry>) -> Unit) {
        val entries = mutableState.value.entries
        if (entries.isEmpty()) return
        action(entries)
        viewModelScope.launch { effectChannel.send(ReaderEffect.Close) }
    }

    private fun loadBody(entry: ThreadEntry) {
        if (entry.key in mutableState.value.messages) return
        viewModelScope.launch {
            loader.load(entry.reference).onSuccess { message ->
                if (message.isUnread) {
                    messagingController.setFlag(entry.reference.accountId, listOf(message.databaseId), Flag.SEEN, true)
                }
                mutableState.update { state ->
                    state.copy(
                        messages = (state.messages + (entry.key to message)).toImmutableMap(),
                        isFlagged = if (entry == state.entries.lastOrNull()) message.isStarred else state.isFlagged,
                        remoteImagesShown = if (message.loadRemoteImages) {
                            (state.remoteImagesShown + entry.key).toImmutableSet()
                        } else {
                            state.remoteImagesShown
                        },
                    )
                }
            }
        }
    }
}

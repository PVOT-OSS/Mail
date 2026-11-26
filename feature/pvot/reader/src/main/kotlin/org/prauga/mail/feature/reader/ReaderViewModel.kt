// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.controller.MessagingControllerWrapper
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
}

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Loaded(val message: ReaderMessage, val showRemoteImages: Boolean) : ReaderUiState
    data object Failed : ReaderUiState
}

class ReaderViewModel(
    private val reference: MessageReference,
    private val loader: MessageLoader,
    private val messagingController: MessagingControllerWrapper,
    private val attachmentOpener: AttachmentOpener,
) : ViewModel() {

    private val effectChannel = Channel<ReaderEffect>(Channel.BUFFERED)
    val effects: Flow<ReaderEffect> = effectChannel.receiveAsFlow()

    fun loadRemoteImages() {
        mutableState.update { state ->
            if (state is ReaderUiState.Loaded) state.copy(showRemoteImages = true) else state
        }
    }

    fun openAttachment(attachment: ReaderAttachment) {
        viewModelScope.launch {
            if (!attachmentOpener.open(reference, attachment)) effectChannel.send(ReaderEffect.AttachmentFailed)
        }
    }

    private val mutableState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val state: StateFlow<ReaderUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = loader.load(reference)
            mutableState.value = result.fold(
                onSuccess = { message ->
                    if (message.isUnread) {
                        messagingController.setFlag(reference.accountId, listOf(message.databaseId), Flag.SEEN, true)
                    }
                    ReaderUiState.Loaded(message, showRemoteImages = message.loadRemoteImages)
                },
                onFailure = { ReaderUiState.Failed },
            )
        }
    }
}

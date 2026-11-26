// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.controller.MessagingControllerWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.thunderbird.core.common.mail.Flag

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Loaded(val message: ReaderMessage) : ReaderUiState
    data object Failed : ReaderUiState
}

class ReaderViewModel(
    private val reference: MessageReference,
    private val loader: MessageLoader,
    private val messagingController: MessagingControllerWrapper,
) : ViewModel() {

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
                    ReaderUiState.Loaded(message)
                },
                onFailure = { ReaderUiState.Failed },
            )
        }
    }
}

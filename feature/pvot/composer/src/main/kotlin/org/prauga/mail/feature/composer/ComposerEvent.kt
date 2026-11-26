// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

sealed interface ComposerEvent {
    data class ChangeTo(val text: String) : ComposerEvent
    data class ChangeCc(val text: String) : ComposerEvent
    data class ChangeBcc(val text: String) : ComposerEvent
    data class ChangeSubject(val text: String) : ComposerEvent
    data class ChangeBody(val text: String) : ComposerEvent
    data class SelectFrom(val index: Int) : ComposerEvent
    data object ShowCcBcc : ComposerEvent
    data object Send : ComposerEvent
}

sealed interface ComposerEffect {
    data object Sent : ComposerEffect
    data class Failed(val reason: String?) : ComposerEffect
}

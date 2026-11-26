// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ReplyDraftTest {
    @Test
    fun `reply subject adds one Re prefix`() {
        assertThat(replySubject("Planning the trip")).isEqualTo("Re: Planning the trip")
        assertThat(replySubject("Re: Planning the trip")).isEqualTo("Re: Planning the trip")
        assertThat(replySubject("AW: Reise")).isEqualTo("Re: Reise")
        assertThat(replySubject("Reading list")).isEqualTo("Re: Reading list")
        assertThat(replySubject(null)).isEqualTo("Re: ")
    }

    @Test
    fun `forward subject adds one Fwd prefix`() {
        assertThat(forwardSubject("Files attached")).isEqualTo("Fwd: Files attached")
        assertThat(forwardSubject("Fwd: Files attached")).isEqualTo("Fwd: Files attached")
        assertThat(forwardSubject("FW: Files attached")).isEqualTo("Fwd: Files attached")
    }

    @Test
    fun `reply references keep the chain and forward references only the message`() {
        val chain = listOf("<a@x>", "<b@x>")

        assertThat(referencesFor("<c@x>", chain, ComposeMode.REPLY)).isEqualTo("<a@x> <b@x> <c@x>")
        assertThat(referencesFor("<c@x>", emptyList(), ComposeMode.REPLY_ALL)).isEqualTo("<c@x>")
        assertThat(referencesFor("<c@x>", chain, ComposeMode.FORWARD)).isEqualTo("<c@x>")
    }
}

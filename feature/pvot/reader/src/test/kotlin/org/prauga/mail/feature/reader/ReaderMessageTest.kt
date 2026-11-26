// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fsck.k9.mail.Address
import kotlin.test.Test

class ReaderMessageTest {
    @Test
    fun `the sender is shown by name, or by address without one`() {
        assertThat(senderNameOf(listOf(Address("alice@example.com", "Alice")))).isEqualTo("Alice")
        assertThat(senderNameOf(listOf(Address("bob@example.com")))).isEqualTo("bob@example.com")
        assertThat(senderNameOf(emptyList())).isEqualTo("")
    }

    @Test
    fun `recipients are listed by name where there is one`() {
        val testSubject = listOf(Address("alice@example.com", "Alice"), Address("bob@example.com"))

        assertThat(recipientsOf(testSubject)).isEqualTo("Alice, bob@example.com")
    }
}

// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test

class RecipientsTest {
    @Test
    fun `blank text has no recipients`() {
        assertThat(recipientsOf("  ")).isEmpty()
    }

    @Test
    fun `comma separated addresses with names are parsed`() {
        val testSubject = recipientsOf("Alice <alice@example.com>, bob@example.org")

        assertThat(testSubject.map { it.address }).containsExactly("alice@example.com", "bob@example.org")
    }

    @Test
    fun `text without an at sign is not a recipient`() {
        assertThat(recipientsOf("alice")).isEmpty()
    }
}

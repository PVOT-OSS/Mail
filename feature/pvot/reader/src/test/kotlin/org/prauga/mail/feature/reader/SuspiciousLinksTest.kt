// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import kotlin.test.Test

class SuspiciousLinksTest {
    @Test
    fun `a link whose text shows another host is suspicious`() {
        val html = """<a href="https://evil.example.net/login">https://bank.example.com/login</a>"""

        val testSubject = suspiciousLinksIn(html)

        assertThat(testSubject).containsOnly(
            "https://evil.example.net/login" to SuspiciousLink("bank.example.com", "evil.example.net"),
        )
    }

    @Test
    fun `a link whose text matches its host is not suspicious`() {
        assertThat(suspiciousLinksIn("""<a href="https://www.example.org/a">example.org</a>""")).isEmpty()
    }

    @Test
    fun `links with ordinary text or other schemes are not checked`() {
        val html = """<a href="https://example.org/offer">Open offer</a>""" +
            """<a href="mailto:a@example.net">b@example.com</a>"""

        assertThat(suspiciousLinksIn(html)).isEmpty()
    }
}

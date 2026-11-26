// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import org.jsoup.Jsoup

class QuoteFoldingTest {
    @Test
    fun `trailing quote is folded with its attribution line`() {
        val html = "<p>Reply</p>Hello<br><br>On Monday, Erin wrote:<br>" +
            "<blockquote class=\"gmail_quote\">Earlier<blockquote>Older</blockquote></blockquote>"

        val result = Jsoup.parse(foldQuotedText(html))

        val details = result.select("details")
        assertThat(details.size).isEqualTo(1)
        assertThat(details.text()).contains("On Monday, Erin wrote:")
        assertThat(details.select("blockquote").size).isEqualTo(2)
        assertThat(result.body().ownText()).isEqualTo("Hello")
    }

    @Test
    fun `plain text keeps the reply and folds only the attribution line`() {
        val html = "<pre>Message 5.\n\nLet's decide.\n\nOn an earlier day, someone wrote:\n" +
            "<blockquote>Earlier message.</blockquote></pre>"

        val result = Jsoup.parse(foldQuotedText(html))

        val details = result.select("details").text()
        assertThat(details).contains("On an earlier day, someone wrote:")
        assertThat(details).doesNotContain("Let's decide.")
        assertThat(result.select("pre").first()?.ownText().orEmpty()).contains("Let's decide.")
    }

    @Test
    fun `attribution at the end of a section div leaves the reply in place`() {
        val html = "<pre><div dir=\"auto\">Message 5.<br><br>Let's decide.<br><br>On Monday, Erin wrote:<br></div>" +
            "<blockquote><div dir=\"auto\">Earlier message.</div></blockquote></pre>"

        val result = Jsoup.parse(foldQuotedText(html))

        assertThat(result.select("details").text()).contains("On Monday, Erin wrote:")
        assertThat(result.select("pre > div").first()?.text().orEmpty()).isEqualTo("Message 5. Let's decide.")
    }

    @Test
    fun `gmail quote container is folded as a whole`() {
        val html = "<div>Thanks</div><div class=\"gmail_quote\"><div>On Monday wrote:</div>" +
            "<blockquote>Earlier</blockquote></div>"

        val result = Jsoup.parse(foldQuotedText(html))

        assertThat(result.select("details > div.gmail_quote > blockquote").size).isEqualTo(1)
    }

    @Test
    fun `quote followed by more reply text stays open`() {
        val html = "<blockquote>Question?</blockquote>Answer."

        val testSubject = foldQuotedText(html)

        assertThat(testSubject).isEqualTo(html)
    }

    @Test
    fun `message without quotes is unchanged`() {
        val html = "<p>Just text</p>"

        val testSubject = foldQuotedText(html)

        assertThat(testSubject).isEqualTo(html)
    }

    @Test
    fun `quote nested in wrappers is folded when nothing follows`() {
        val html = "<div><p>Reply</p><div><blockquote>Earlier</blockquote> </div></div>"

        val result = Jsoup.parse(foldQuotedText(html))

        assertThat(result.select("details blockquote").isNotEmpty()).isTrue()
        assertThat(result.select("details > summary").text()).isEqualTo("•••")
    }
}

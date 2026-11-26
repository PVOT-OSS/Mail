// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import java.net.URI
import java.net.URISyntaxException
import org.jsoup.Jsoup

data class SuspiciousLink(val shownHost: String, val realHost: String)

private val looksLikeWebAddress = Regex("""^(https?://)?([a-z0-9-]+\.)+[a-z]{2,}(/\S*)?$""", RegexOption.IGNORE_CASE)

internal fun suspiciousLinksIn(html: String): Map<String, SuspiciousLink> =
    Jsoup.parse(html).select("a[href]").mapNotNull { anchor ->
        val href = anchor.attr("href")
        val realHost = hostOf(href) ?: return@mapNotNull null
        val shown = anchor.text().trim()
        if (!looksLikeWebAddress.matches(shown)) return@mapNotNull null
        val shownHost = hostOf(if (shown.contains("://")) shown else "https://$shown") ?: return@mapNotNull null
        if (shownHost.withoutWww() == realHost.withoutWww()) null else href to SuspiciousLink(shownHost, realHost)
    }.toMap()

private fun hostOf(url: String): String? = try {
    URI(url).takeIf { it.scheme == "http" || it.scheme == "https" }?.host?.lowercase()
} catch (_: URISyntaxException) {
    null
}

private fun String.withoutWww(): String = removePrefix("www.")

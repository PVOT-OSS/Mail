// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

private const val QUOTE_SELECTOR = "div.gmail_quote, blockquote"
private const val MAX_ATTRIBUTION_LENGTH = 200
private const val SUMMARY_STYLE = "list-style: none; display: inline-block; cursor: pointer; " +
    "padding: 0 10px; margin: 8px 0; border-radius: 10px; letter-spacing: 2px; " +
    "background: rgba(128, 128, 128, 0.25);"

internal fun foldQuotedText(html: String): String {
    val document = Jsoup.parse(html)
    document.outputSettings().prettyPrint(false)
    val quote = document.select(QUOTE_SELECTOR)
        .firstOrNull { it.parents().none { parent -> parent.`is`(QUOTE_SELECTOR) } && it.isTrailing() }
        ?: return html
    val attribution = quote.attributionNodes()
    val details = Element("details").appendChild(Element("summary").attr("style", SUMMARY_STYLE).text("•••"))
    quote.before(details)
    attribution.forEach { details.appendChild(it) }
    details.appendChild(quote)
    return document.outerHtml()
}

private fun Element.isTrailing(): Boolean {
    var element: Element? = this
    while (element != null && element.tagName() != "body") {
        if (element.siblingNodesAfter().any { it.hasVisibleContent() }) return false
        element = element.parent()
    }
    return true
}

private fun Node.siblingNodesAfter(): List<Node> = generateSequence(nextSibling()) { it.nextSibling() }.toList()

private fun Node.hasVisibleContent(): Boolean = when (this) {
    is TextNode -> !isBlank
    is Element -> tagName() == "img" || text().isNotBlank()
    else -> false
}

private fun Element.attributionNodes(): List<Node> {
    val line = lastContentNodeBefore()?.let { if (it is TextNode) it.lastLine() else it }
    if (line == null || !line.isAttribution()) return emptyList()
    return listOf(line) + line.siblingNodesAfter().takeWhile { it.isLineBreakOrBlank() }
}

private fun Node.lastContentNodeBefore(): Node? {
    var current = previousSibling()
    while (current != null) {
        current = when {
            current.isLineBreakOrBlank() -> current.previousSibling()
            current is Element && current.childNodeSize() > 0 -> current.childNode(current.childNodeSize() - 1)
            else -> return current
        }
    }
    return null
}

private fun TextNode.lastLine(): TextNode {
    val lineStart = wholeText.trimEnd().lastIndexOf('\n') + 1
    return if (lineStart > 0) splitText(lineStart) else this
}

private fun Node.isLineBreakOrBlank(): Boolean =
    (this is TextNode && isBlank) || (this is Element && tagName() == "br")

private fun Node.isAttribution(): Boolean {
    val text = when (this) {
        is TextNode -> text()
        is Element -> text()
        else -> ""
    }.trim()
    return text.endsWith(":") && text.length <= MAX_ATTRIBUTION_LENGTH
}

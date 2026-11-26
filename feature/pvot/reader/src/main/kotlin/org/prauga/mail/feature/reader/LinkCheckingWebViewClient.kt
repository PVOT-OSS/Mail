// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

internal class LinkCheckingWebViewClient(
    private val base: WebViewClient,
    private val suspiciousLinks: Map<String, SuspiciousLink>,
    private val onSuspiciousLink: (url: String, link: SuspiciousLink) -> Unit,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        val suspicious = suspiciousLinks[url]
        return if (suspicious != null) {
            onSuspiciousLink(url, suspicious)
            true
        } else {
            base.shouldOverrideUrlLoading(view, request)
        }
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
        base.shouldInterceptRequest(view, request)

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) = base.onPageStarted(view, url, favicon)

    override fun onPageFinished(view: WebView, url: String?) = base.onPageFinished(view, url)

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) =
        base.onReceivedError(view, request, error)
}

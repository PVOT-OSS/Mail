// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.fsck.k9.view.MessageWebView
import com.fsck.k9.view.WebViewConfigProvider
import org.koin.compose.koinInject

@Composable
internal fun ReaderBody(
    message: ReaderMessage,
    showRemoteImages: Boolean,
    onSuspiciousLink: (url: String, link: SuspiciousLink) -> Unit,
    modifier: Modifier = Modifier,
    webViewConfigProvider: WebViewConfigProvider = koinInject(),
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MessageWebView(context).apply {
                configure(webViewConfigProvider.createForMessageView())
                setBackgroundColor(Color.TRANSPARENT)
            }
        },
        update = { webView ->
            webView.blockNetworkData(!showRemoteImages)
            webView.displayHtmlContentWithInlineAttachments(message.html, message.attachmentResolver, null)
            webView.webViewClient =
                LinkCheckingWebViewClient(webView.webViewClient, message.suspiciousLinks, onSuspiciousLink)
        },
        onRelease = { webView -> webView.destroy() },
    )
}

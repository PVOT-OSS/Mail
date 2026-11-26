// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.fsck.k9.view.MessageWebView
import com.fsck.k9.view.WebViewConfigProvider
import org.koin.compose.koinInject

@Composable
internal fun ReaderBody(
    message: ReaderMessage,
    modifier: Modifier = Modifier,
    webViewConfigProvider: WebViewConfigProvider = koinInject(),
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MessageWebView(context).apply {
                configure(webViewConfigProvider.createForMessageView())
                blockNetworkData(true)
            }
        },
        update = { webView ->
            webView.displayHtmlContentWithInlineAttachments(message.html, message.attachmentResolver, null)
        },
        onRelease = { webView -> webView.destroy() },
    )
}

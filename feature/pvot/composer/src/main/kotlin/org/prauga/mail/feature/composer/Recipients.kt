// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.feature.composer

import com.fsck.k9.mail.Address

internal fun recipientsOf(text: String): List<Address> =
    Address.parseUnencoded(text).filter { it.address.orEmpty().contains('@') }

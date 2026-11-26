// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.core.mailactions

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

class PendingMailActionsTest {
    private val scope = TestScope()
    private val testSubject = PendingMailActions(scope, commitDelayMillis = 1_000)
    private var performed = 0

    @Test
    fun `a pending action hides its messages`() {
        testSubject.add(MailActionKind.ARCHIVE, setOf(1, 2)) { performed++ }

        assertThat(listOf(1L, 2L, 3L).withoutPending(testSubject.pending.value) { it }).containsExactly(3L)
    }

    @Test
    fun `undo drops the action without performing it`() {
        val action = testSubject.add(MailActionKind.DELETE, setOf(1)) { performed++ }

        testSubject.undo(action.id)
        scope.advanceTimeBy(2_000)

        assertThat(testSubject.pending.value).isEmpty()
        assertThat(performed).isEqualTo(0)
    }

    @Test
    fun `commit performs the action once`() {
        val action = testSubject.add(MailActionKind.ARCHIVE, setOf(1)) { performed++ }

        testSubject.commit(action.id)
        testSubject.commit(action.id)
        scope.advanceTimeBy(2_000)

        assertThat(performed).isEqualTo(1)
        assertThat(testSubject.pending.value).isEmpty()
    }

    @Test
    fun `an action nobody resolves is performed after the delay`() {
        testSubject.add(MailActionKind.ARCHIVE, setOf(1)) { performed++ }

        scope.advanceTimeBy(1_001)
        scope.runCurrent()

        assertThat(performed).isEqualTo(1)
    }
}

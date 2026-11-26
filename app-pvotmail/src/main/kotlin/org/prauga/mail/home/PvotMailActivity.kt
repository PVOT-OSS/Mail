// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fsck.k9.ui.settings.SettingsActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme
import org.prauga.mail.navigation.PvotMailNavDisplay

class PvotMailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PvotAppTheme {
                PvotMailNavDisplay(onOpenSettings = { SettingsActivity.launch(this) })
            }
        }
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, PvotMailActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

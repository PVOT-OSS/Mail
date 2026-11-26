// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

plugins {
    id(ThunderbirdPlugins.Library.androidCompose)
}

android {
    namespace = "org.prauga.mail.feature.reader"
    resourcePrefix = "reader_"
    compileSdk { version = release(37) { minorApiLevel = 1 } }

    defaultConfig {
        minSdk = 27
    }
}

dependencies {
    implementation(projects.core.android.account)
    implementation(projects.core.android.webkit)
    implementation(projects.core.common)
    implementation(projects.feature.account.api)
    implementation(projects.feature.mail.message.reader.api)
    implementation(projects.legacy.core)
    implementation(projects.legacy.message)
    implementation(projects.legacy.ui.legacy)
    implementation(projects.mail.common)

    implementation(libs.pvot.design.system)
    implementation(libs.pvot.core.ui)
}

codeCoverage {
    branchCoverage = 0
    lineCoverage = 0
}

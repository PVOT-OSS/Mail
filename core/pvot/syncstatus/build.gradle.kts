// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

plugins {
    id(ThunderbirdPlugins.Library.androidCompose)
}

android {
    namespace = "org.prauga.mail.core.syncstatus"
    resourcePrefix = "syncstatus_"
    compileSdk { version = release(37) { minorApiLevel = 1 } }

    defaultConfig {
        minSdk = 27
    }
}

dependencies {
    implementation(projects.feature.account.api)
    implementation(projects.legacy.message)
}

codeCoverage {
    branchCoverage = 0
    lineCoverage = 0
}

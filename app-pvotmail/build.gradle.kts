// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

plugins {
    id(ThunderbirdPlugins.App.androidCompose)
}

val testCoverageEnabled = providers
    .gradleProperty("testCoverageEnabled")
    .isPresent

android {
    namespace = "org.prauga.mail"
    compileSdk { version = release(37) { minorApiLevel = 1 } }

    defaultConfig {
        applicationId = "org.prauga.mail"
        testApplicationId = "org.prauga.mail.tests"

        minSdk = 27

        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "CLIENT_INFO_APP_NAME", "\"Pvot Mail\"")
    }

    androidResources {
        localeFilters += listOf("en")
    }

    buildTypes {
        val minify = providers.gradleProperty("minify")
            .map(String::toBoolean)
            .orElse(true)
        release {
            isMinifyEnabled = minify.get()
            isShrinkResources = minify.get()

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        debug {
            applicationIdSuffix = ".dev"

            enableUnitTestCoverage = testCoverageEnabled
            enableAndroidTestCoverage = testCoverageEnabled

            isMinifyEnabled = false
        }
    }

    packaging {
        jniLibs {
            excludes += listOf("kotlin/**")
        }

        resources {
            excludes += listOf(
                "META-INF/*.kotlin_module",
                "META-INF/*.version",
                "kotlin/**",
                "DebugProbesKt.bin",
            )
        }
    }
}

dependencies {
    implementation(projects.appCommon)
    implementation(projects.core.ui.compose.common)
    implementation(projects.core.ui.legacy.theme2.common)
    implementation(projects.feature.launcher)
    implementation(projects.feature.pvot.mailboxes)
    implementation(projects.feature.search.implLegacy)
    implementation(projects.feature.mail.folder.api)
    implementation(projects.feature.account.settings.api)
    implementation(projects.core.android.account)
    implementation(projects.feature.mail.message.list.api)
    implementation(projects.feature.mail.message.list.internal)
    implementation(projects.feature.mail.message.reader.api)

    implementation(projects.legacy.core)
    implementation(projects.legacy.ui.legacy)

    implementation(projects.core.featureflag)

    implementation(projects.feature.autodiscovery.api)
    implementation(projects.feature.account.settings.impl)

    implementation(projects.feature.funding.noop)
    implementation(projects.feature.migration.launcher.noop)
    implementation(projects.feature.onboarding.migration.noop)
    implementation(projects.feature.thundermail.api)
    implementation(projects.feature.thundermail.k9mail)
    implementation(projects.feature.telemetry.noop)
    implementation(projects.feature.widget.messageList)
    implementation(projects.feature.widget.messageListGlance)
    implementation(projects.feature.widget.shortcut)
    implementation(projects.feature.widget.unread)

    implementation(libs.pvot.design.system)
    implementation(libs.pvot.design.system.views)

    implementation(libs.androidx.work.runtime)

    debugImplementation(projects.backend.demo)
    debugImplementation(projects.feature.autodiscovery.demo)

    testImplementation(projects.feature.account.api)
    testImplementation(projects.feature.account.common)
    testImplementation(projects.feature.thundermail.internal.common)
    testImplementation(projects.plugins.openpgpApiLib.openpgpApi)
    testImplementation(projects.feature.changelog.api)
    testImplementation(projects.feature.changelog.internal)

    testImplementation(libs.appauth)
}

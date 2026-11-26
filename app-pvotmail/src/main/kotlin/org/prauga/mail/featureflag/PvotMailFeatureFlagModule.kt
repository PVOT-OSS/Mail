// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.mail.featureflag

import net.thunderbird.core.featureflag.inject.featureFlagModule
import net.thunderbird.core.featureflag.model.AppVariantOverrides
import net.thunderbird.core.featureflag.model.EmptyAppVariantOverride
import net.thunderbird.core.featureflag.serialization.FlagRegistryOverrideSerializer
import org.koin.dsl.module

internal val pvotMailFeatureFlagModule = module {
    includes(featureFlagModule)
    factory<AppVariantOverrides.Factory> { EmptyAppVariantOverride }
    factory {
        FlagRegistryOverrideSerializer(
            k9Factory = EmptyAppVariantOverride,
            thunderbirdFactory = EmptyAppVariantOverride,
        )
    }
}

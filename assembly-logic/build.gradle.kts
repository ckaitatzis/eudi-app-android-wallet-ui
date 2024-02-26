/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */
import eu.europa.ec.euidi.config.LibraryModule
import eu.europa.ec.euidi.kover.KoverExclusionRules
import eu.europa.ec.euidi.kover.excludeFromKoverReport
import eu.europa.ec.euidi.kover.koverModules

plugins {
    id("eudi.android.library")
    id("eudi.android.library.compose")
}

android {
    namespace = "eu.europa.ec.assemblylogic"

    defaultConfig {
        // App name
        manifestPlaceholders["appName"] = "EUDI Wallet"
    }
}

moduleConfig {
    module = LibraryModule.AssemblyLogic
}

dependencies {

    // Logic Modules
    api(project(":resources-logic"))
    api(project(":business-logic"))
    api(project(":ui-logic"))
    api(project(":network-logic"))
    api(project(":analytics-logic"))

    // Feature Modules
    api(project(":common-feature"))
    api(project(":startup-feature"))
    api(project(":login-feature"))
    api(project(":dashboard-feature"))
    api(project(":presentation-feature"))
    api(project(":proximity-feature"))
    api(project(":issuance-feature"))

    //Test Cover Report
    koverModules.forEach {
        kover(project(it.key.path)) {
            excludeFromKoverReport(
                excludedClasses = it.value.classes,
                excludedPackages = it.value.packages,
            )
        }
    }
}

excludeFromKoverReport(
    excludedClasses = KoverExclusionRules.AssemblyLogic.classes,
    excludedPackages = KoverExclusionRules.AssemblyLogic.packages,
)
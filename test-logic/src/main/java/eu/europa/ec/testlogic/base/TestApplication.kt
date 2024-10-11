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

package eu.europa.ec.testlogic.base

import android.app.Application
import eu.europa.ec.resourceslogic.theme.ThemeManager
import eu.europa.ec.resourceslogic.theme.templates.ThemeDimensTemplate
import eu.europa.ec.resourceslogic.theme.values.ThemeColors
import eu.europa.ec.resourceslogic.theme.values.ThemeShapes
import eu.europa.ec.resourceslogic.theme.values.ThemeTypography

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeTheme()
    }

    private fun initializeTheme() {
        ThemeManager.Builder()
            .withLightColors(ThemeColors.lightColors)
            .withDarkColors(ThemeColors.darkColors)
            .withTypography(ThemeTypography.typo)
            .withShapes(ThemeShapes.shapes)
            .withDimensions(
                ThemeDimensTemplate(
                    screenPadding = 10.0
                )
            )
            .build()
    }
}
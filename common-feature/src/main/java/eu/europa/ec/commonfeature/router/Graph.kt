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

package eu.europa.ec.commonfeature.router

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import eu.europa.ec.businesslogic.BuildConfig
import eu.europa.ec.commonfeature.config.BiometricUiConfig
import eu.europa.ec.commonfeature.config.SuccessUIConfig
import eu.europa.ec.commonfeature.ui.biometric.BiometricScreen
import eu.europa.ec.commonfeature.ui.success.SuccessScreen
import eu.europa.ec.uilogic.navigation.CommonScreens
import eu.europa.ec.uilogic.navigation.ModuleRoute
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.featureCommonGraph(navController: NavController) {
    navigation(
        startDestination = CommonScreens.Biometric.screenRoute,
        route = ModuleRoute.CommonModule.route
    ) {
        composable(
            route = CommonScreens.Biometric.screenRoute,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        BuildConfig.DEEPLINK + CommonScreens.Biometric.screenRoute
                }
            ),
            arguments = listOf(
                navArgument(BiometricUiConfig.serializedKeyName) {
                    type = NavType.StringType
                }
            )
        ) {
            BiometricScreen(
                navController,
                getViewModel(
                    parameters = {
                        parametersOf(
                            it.arguments?.getString(BiometricUiConfig.serializedKeyName).orEmpty()
                        )
                    }
                )
            )
        }
        composable(
            route = CommonScreens.Success.screenRoute,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        BuildConfig.DEEPLINK + CommonScreens.Success.screenRoute
                }
            ),
            arguments = listOf(
                navArgument(SuccessUIConfig.serializedKeyName) {
                    type = NavType.StringType
                }
            )
        ) {
            SuccessScreen(
                navController,
                getViewModel(
                    parameters = {
                        parametersOf(
                            it.arguments?.getString(SuccessUIConfig.serializedKeyName).orEmpty()
                        )
                    }
                )
            )
        }
    }
}
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

package eu.europa.ec.proximityfeature.router

import ProximityRequestScreen
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import eu.europa.ec.businesslogic.BuildConfig
import eu.europa.ec.proximityfeature.ui.loading.ProximityLoadingScreen
import eu.europa.ec.proximityfeature.ui.qr.ProximityQRScreen
import eu.europa.ec.uilogic.navigation.ModuleRoute
import eu.europa.ec.uilogic.navigation.ProximityScreens
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.featureProximityGraph(navController: NavController) {
    navigation(
        startDestination = ProximityScreens.QR.screenRoute,
        route = ModuleRoute.ProximityModule.route
    ) {
        // QR
        composable(
            route = ProximityScreens.QR.screenRoute,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        BuildConfig.DEEPLINK + ProximityScreens.QR.screenRoute
                }
            )
        ) {
            ProximityQRScreen(
                navController,
                koinViewModel()
            )
        }

        // Request
        composable(
            route = ProximityScreens.Request.screenRoute,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        BuildConfig.DEEPLINK + ProximityScreens.Request.screenRoute
                }
            )
        ) {
            ProximityRequestScreen(
                navController,
                koinViewModel()
            )
        }

        // Loading
        composable(
            route = ProximityScreens.Loading.screenRoute,
        ) {
            ProximityLoadingScreen(
                navController,
                koinViewModel()
            )
        }
    }
}
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

package eu.europa.ec.uilogic.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import eu.europa.ec.uilogic.config.ConfigUILogic
import eu.europa.ec.uilogic.controller.AnalyticsController

interface RouterHost {
    fun getNavController(): NavHostController
    fun getNavContext(): Context
    fun currentFlowIsAfterOnBoarding(): Boolean
    fun popToLandingScreen()
    fun getLandingScreen(): String

    @Composable
    fun StartFlow(builder: NavGraphBuilder.(NavController) -> Unit)
}

class RouterHostImpl constructor(
    private val configUILogic: ConfigUILogic,
    private val analyticsController: AnalyticsController
) : RouterHost {

    private lateinit var navController: NavHostController
    private lateinit var context: Context

    override fun getNavController(): NavHostController = navController
    override fun getNavContext(): Context = context

    @Composable
    override fun StartFlow(builder: NavGraphBuilder.(NavController) -> Unit) {
        navController = rememberNavController()
        context = LocalContext.current
        NavHost(
            navController = navController,
            startDestination = ModuleRoute.StartupModule.route
        ) {
            builder(navController)
        }

        DisposableEffect(key1 = navController) {
            val destinationChangedListener =
                NavController.OnDestinationChangedListener { _, destination, _ ->
                    destination.route?.let { route ->
                        println("Giannis $route")
                        analyticsController.logScreen(route)
                    }
                }
            navController.addOnDestinationChangedListener(destinationChangedListener)

            onDispose {
                navController.removeOnDestinationChangedListener(destinationChangedListener)
            }
        }

        LaunchedEffect(key1 = navController) {
            navController.currentBackStack.collect { entries ->
                println("Giannis entries:")
                entries.forEach { entry ->
                    val arguments = entry.destination.arguments
                    println("\tGiannis ${entry.destination.route} is ${entry.lifecycle.currentState} with args: $arguments")
                }
            }
        }

    }

    override fun currentFlowIsAfterOnBoarding(): Boolean {
        val screenRoute = getLandingScreen()
        try {
            if (navController.currentDestination?.route == screenRoute) {
                return true
            }
            navController.getBackStackEntry(screenRoute)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun popToLandingScreen() {
        navController.popBackStack(
            route = getLandingScreen(),
            inclusive = false
        )
    }

    override fun getLandingScreen(): String {
        return configUILogic.landingScreenIdentifier.screenRoute
    }
}
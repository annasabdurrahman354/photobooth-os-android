package com.askara.photobooth.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.askara.photobooth.ui.screen.LoginScreen
import com.askara.photobooth.ui.screen.BoothSelectScreen
import com.askara.photobooth.ui.screen.TemplateSelectScreen
import com.askara.photobooth.ui.screen.SessionScreen
import com.askara.photobooth.ui.theme.BrutalStyle
import com.askara.photobooth.ui.theme.Slate50
import com.askara.photobooth.ui.theme.Slate950
import com.askara.photobooth.viewmodel.AuthViewModel
import com.askara.photobooth.viewmodel.BoothSelectViewModel
import com.askara.photobooth.viewmodel.TemplateSelectViewModel
import com.askara.photobooth.viewmodel.SessionViewModel

@Composable
fun NavGraph(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Slate50,
        shape = BrutalStyle.CardShape,
        border = BorderStroke(BrutalStyle.AppFrame, Slate950)
    ) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate("booths/${authViewModel.uiState.value.profile?.tenant_id ?: ""}") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "booths/{tenantId}",
                arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tenantId = backStackEntry.arguments?.getString("tenantId") ?: ""
                val viewModel: BoothSelectViewModel = viewModel()

                BoothSelectScreen(
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    tenantId = tenantId,
                    onBoothSelected = { booth ->
                        navController.navigate("templates/${tenantId}/${booth.id}")
                    },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "templates/{tenantId}/{boothId}",
                arguments = listOf(
                    navArgument("tenantId") { type = NavType.StringType },
                    navArgument("boothId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tenantId = backStackEntry.arguments?.getString("tenantId") ?: ""
                val boothId = backStackEntry.arguments?.getString("boothId") ?: ""
                val viewModel: TemplateSelectViewModel = viewModel()

                TemplateSelectScreen(
                    viewModel = viewModel,
                    tenantId = tenantId,
                    boothId = boothId,
                    onTemplateSelected = { template ->
                        val slotCount = template.layout_json?.let { extractSlotCount(it) } ?: 1
                        navController.navigate("session/${boothId}/${template.id}/${slotCount}")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "session/{boothId}/{templateId}/{totalSlots}",
                arguments = listOf(
                    navArgument("boothId") { type = NavType.StringType },
                    navArgument("templateId") { type = NavType.StringType },
                    navArgument("totalSlots") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val boothId = backStackEntry.arguments?.getString("boothId") ?: ""
                val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
                val totalSlots = backStackEntry.arguments?.getInt("totalSlots") ?: 1

                val sessionViewModel: SessionViewModel = viewModel()
                SessionScreen(
                    viewModel = sessionViewModel,
                    boothId = boothId,
                    templateId = templateId,
                    totalSlots = totalSlots,
                    onDone = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

private fun extractSlotCount(layoutJson: String): Int {
    return try {
        layoutJson.split("\"type\":\"photo\"").size - 1
    } catch (e: Exception) {
        1
    }
}

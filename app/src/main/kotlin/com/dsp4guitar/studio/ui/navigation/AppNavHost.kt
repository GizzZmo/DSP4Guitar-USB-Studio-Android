package com.dsp4guitar.studio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dsp4guitar.studio.audio.AudioEngineJni
import com.dsp4guitar.studio.ui.screen.ChainScreen
import com.dsp4guitar.studio.ui.screen.SettingsScreen

object Routes {
    const val CHAIN    = "chain"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost(audioEngine: AudioEngineJni) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CHAIN
    ) {
        composable(Routes.CHAIN) {
            ChainScreen(
                audioEngine  = audioEngine,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                audioEngine = audioEngine,
                onBack      = { navController.popBackStack() }
            )
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.game.GamePhase
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecordsScreen
import com.example.ui.theme.CandySnakeTheme
import com.example.viewmodel.CandySnakeViewModel

const val ROUTE_HOME = "home"
const val ROUTE_GAME = "game"
const val ROUTE_RECORDS = "records"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        setContent {
            CandySnakeTheme {
                CandySnakeApp()
            }
        }
    }
}

@Composable
fun CandySnakeApp() {
    val context = LocalContext.current
    val viewModel: CandySnakeViewModel = viewModel(
        factory = remember(context) { CandySnakeViewModel.Factory(context) }
    )
    val navController = rememberNavController()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val classicBest by viewModel.classicBestScore.collectAsStateWithLifecycle()
    val endlessBest by viewModel.endlessBestScore.collectAsStateWithLifecycle()
    val classicRecords by viewModel.classicRecords.collectAsStateWithLifecycle()
    val endlessRecords by viewModel.endlessRecords.collectAsStateWithLifecycle()
    val careerStats by viewModel.careerStats.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = ROUTE_HOME,
    ) {
        composable(ROUTE_HOME) {
            HomeScreen(
                settings = settings,
                classicBest = classicBest,
                endlessBest = endlessBest,
                onSelectMode = viewModel::selectMode,
                onSelectSkin = viewModel::selectSkin,
                onStartGame = { mode ->
                    viewModel.selectMode(mode)
                    viewModel.prepareNewGame(mode)
                    navController.navigate(ROUTE_GAME)
                },
                onOpenRecords = {
                    navController.navigate(ROUTE_RECORDS)
                },
                onOpenSettings = {
                    showSettingsDialog = true
                },
            )
        }

        composable(ROUTE_GAME) {
            GameScreen(
                state = gameState,
                settings = settings,
                onDirection = viewModel::onDirectionInput,
                onBoardTap = {
                    if (gameState.phase == GamePhase.READY) {
                        viewModel.startFromReady()
                    }
                },
                onTogglePause = viewModel::togglePause,
                onResume = viewModel::resumeGame,
                onRestart = viewModel::restartGame,
                onCycleControlMode = viewModel::cycleControlMode,
                onSelectControlMode = viewModel::setControlMode,
                onToggleSound = viewModel::toggleSound,
                onOpenRecords = {
                    navController.navigate(ROUTE_RECORDS)
                },
                onBackHome = {
                    viewModel.pauseGame()
                    navController.popBackStack(ROUTE_HOME, inclusive = false)
                },
            )
        }

        composable(ROUTE_RECORDS) {
            RecordsScreen(
                initialMode = settings.selectedMode,
                classicRecords = classicRecords,
                endlessRecords = endlessRecords,
                careerStats = careerStats,
                onStartGame = { mode ->
                    viewModel.selectMode(mode)
                    viewModel.prepareNewGame(mode)
                    navController.navigate(ROUTE_GAME) {
                        popUpTo(ROUTE_HOME)
                    }
                },
                onClearRecords = viewModel::clearAllRecords,
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onControlModeSelected = viewModel::setControlMode,
            onSpeedSelected = viewModel::setSpeedPreset,
            onSoundToggle = viewModel::setSoundEnabled,
            onHapticsToggle = viewModel::setHapticsEnabled,
            onDismiss = { showSettingsDialog = false },
        )
    }
}

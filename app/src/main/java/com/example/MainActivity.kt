package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.challenges.ChallengesScreen
import com.example.ui.flight.FlightScreen
import com.example.ui.hangar.HangarScreen
import com.example.ui.leaderboard.LeaderboardScreen
import com.example.ui.multiplayer.MultiplayerLobbyScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.upgrades.UpgradesScreen
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                        .safeDrawingPadding()
                ) {
                    when (currentScreen) {
                        AppScreen.HANGAR -> HangarScreen(viewModel = viewModel)
                        AppScreen.FLIGHT -> FlightScreen(viewModel = viewModel)
                        AppScreen.MULTIPLAYER -> MultiplayerLobbyScreen(viewModel = viewModel)
                        AppScreen.UPGRADES -> UpgradesScreen(viewModel = viewModel)
                        AppScreen.CHALLENGES -> ChallengesScreen(viewModel = viewModel)
                        AppScreen.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

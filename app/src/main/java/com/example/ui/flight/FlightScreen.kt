package com.example.ui.flight

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun FlightScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val targets by viewModel.targets.collectAsState()
    val samMissiles by viewModel.samMissiles.collectAsState()
    val flakBullets by viewModel.flakBullets.collectAsState()
    val explosions by viewModel.explosions.collectAsState()
    val competitors by viewModel.competitors.collectAsState()

    val throttle by viewModel.throttleInput.collectAsState()
    val isArmed by viewModel.isArmed.collectAsState()
    val isThermal by viewModel.isThermalMode.collectAsState()
    val equipment by viewModel.equipment.collectAsState()
    val missionResult by viewModel.missionResult.collectAsState()

    // Handle back button to safely return to hangar
    BackHandler {
        viewModel.navigateTo(AppScreen.HANGAR)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. 3D World Canvas View
        FpvCanvasView(
            physics = viewModel.physics,
            targets = targets,
            samMissiles = samMissiles,
            flakBullets = flakBullets,
            explosions = explosions,
            competitors = competitors,
            isThermalMode = isThermal
        )

        // 2. FPV OSD Overlay (HUD)
        FpvOsdOverlay(
            physics = viewModel.physics,
            targets = targets,
            isThermalMode = isThermal
        )

        // 3. Virtual Flight Controls (Joysticks & Action buttons)
        FlightControls(
            throttle = throttle,
            isAcro = equipment.isAcroMode,
            isThermal = isThermal,
            isArmed = isArmed,
            onThrottleChange = { viewModel.throttleInput.value = it },
            onYawChange = { viewModel.yawInput.value = it },
            onPitchChange = { viewModel.pitchInput.value = it },
            onRollChange = { viewModel.rollInput.value = it },
            onToggleAcro = { viewModel.toggleAcroMode() },
            onToggleThermal = { viewModel.isThermalMode.value = !viewModel.isThermalMode.value },
            onToggleArm = { viewModel.isArmed.value = !viewModel.isArmed.value }
        )

        // 4. Abort / Back button top left
        IconButton(
            onClick = { viewModel.navigateTo(AppScreen.HANGAR) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0x55000000), CircleShape)
                .testTag("abort_sortie_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Прервать вылет",
                tint = Color.White
            )
        }

        // 5. Impact & Combat Result Dialog
        missionResult?.let { res ->
            ImpactResultDialog(
                result = res,
                onRestartFlight = {
                    viewModel.startFlight(viewModel.isMultiplayerSortie.value)
                },
                onReturnHangar = {
                    viewModel.navigateTo(AppScreen.HANGAR)
                }
            )
        }
    }
}

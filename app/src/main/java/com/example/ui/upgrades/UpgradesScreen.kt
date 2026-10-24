package com.example.ui.upgrades

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ModuleType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalOrange
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextLight
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun UpgradesScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val warPoints by viewModel.warPoints.collectAsState()
    val repo = viewModel.repository

    BackHandler {
        viewModel.navigateTo(AppScreen.HANGAR)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HANGAR) },
                    modifier = Modifier.testTag("back_to_hangar_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = "МОДЕРНИЗАЦИЯ МОДУЛЕЙ",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Улучшение тактико-технических характеристик дрона",
                        color = TextDim,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                // WP Balance
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1B2A1C), RoundedCornerShape(6.dp))
                        .border(1.dp, RadarGreen, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("WP", color = RadarGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "$warPoints",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Upgrades List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ModuleType.values()) { module ->
                    val currentLevel = repo.getModuleLevel(module)
                    val isMax = currentLevel >= module.maxLevel
                    val upgradeCost = module.baseCostWP * currentLevel
                    val canAfford = warPoints >= upgradeCost && !isMax

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("module_card_${module.name}"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon badge
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(DarkSurfaceVariant, CircleShape)
                                    .border(1.dp, TacticalOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (module) {
                                    ModuleType.MOTOR_THRUST -> Icons.Default.Speed
                                    ModuleType.BATTERY_CAPACITY -> Icons.Default.BatteryChargingFull
                                    ModuleType.EW_SHIELDING -> Icons.Default.Security
                                    ModuleType.WARHEAD_YIELD -> Icons.Default.SportsScore
                                    ModuleType.THERMAL_OPTICS -> Icons.Default.CameraAlt
                                }
                                Icon(icon, contentDescription = null, tint = TacticalOrange, modifier = Modifier.size(24.dp))
                            }

                            Spacer(Modifier.width(14.dp))

                            // Info & Progress
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = module.title,
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "LVL $currentLevel / ${module.maxLevel}",
                                        color = if (isMax) RadarGreen else TacticalAmber,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = module.description,
                                    color = TextDim,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )

                                // Level progress bar
                                LinearProgressIndicator(
                                    progress = { currentLevel.toFloat() / module.maxLevel },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(6.dp),
                                    color = TacticalOrange,
                                    trackColor = Color(0xFF263238),
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            // Upgrade button
                            if (isMax) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF1E3A24), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = RadarGreen, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("МАКСИМУМ", color = RadarGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.upgradeModule(module) },
                                    enabled = canAfford,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TacticalOrange,
                                        disabledContainerColor = Color(0xFF2A3440)
                                    ),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("upgrade_button_${module.name}")
                                ) {
                                    Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "$upgradeCost WP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (canAfford) Color.Black else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

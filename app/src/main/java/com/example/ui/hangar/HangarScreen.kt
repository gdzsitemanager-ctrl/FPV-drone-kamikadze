package com.example.ui.hangar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BatteryTypeId
import com.example.model.DroneFrameId
import com.example.model.MotorTypeId
import com.example.model.SkinId
import com.example.model.WarheadType
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
fun HangarScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val equipment by viewModel.equipment.collectAsState()
    val warPoints by viewModel.warPoints.collectAsState()
    val tokens by viewModel.tokens.collectAsState()
    val repo = viewModel.repository

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("БОЕВАЯ ЧАСТЬ", "РАМА ДРОНА", "ДВИГАТЕЛИ И АКБ", "КАМУФЛЯЖ")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar: Callsign, WP, Tokens, Fast Nav
            HangarTopBar(
                callsign = repo.callsign,
                warPoints = warPoints,
                tokens = tokens,
                onNavigate = { viewModel.navigateTo(it) }
            )

            // Main 16:9 Landscape Split Content: Left = Drone 3D/Diagram Card, Right = Customization & Sortie
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Pane: Drone Visual Card & Telemetry
                DronePreviewPane(
                    equipment = equipment,
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight()
                )

                // Right Pane: Customization Tabs & Launch Buttons
                Card(
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Customization Tabs
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = DarkSurfaceVariant,
                            contentColor = TacticalOrange,
                            edgePadding = 8.dp,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = TacticalOrange,
                                    height = 3.dp
                                )
                            }
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = if (selectedTab == index) TacticalOrange else TextDim
                                        )
                                    },
                                    modifier = Modifier.testTag("hangar_tab_$index")
                                )
                            }
                        }

                        // Tab Content
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            when (selectedTab) {
                                0 -> WarheadSelector(
                                    current = equipment.warhead,
                                    isUnlocked = { repo.isWarheadUnlocked(it) },
                                    canAfford = { warPoints >= it.priceWP },
                                    onSelect = { viewModel.equipWarhead(it) }
                                )
                                1 -> FrameSelector(
                                    current = equipment.frame,
                                    isUnlocked = { repo.isFrameUnlocked(it) },
                                    canAfford = { warPoints >= it.priceWP },
                                    onSelect = { viewModel.equipFrame(it) }
                                )
                                2 -> MotorBatterySelector(
                                    currentMotor = equipment.motor,
                                    currentBattery = equipment.battery,
                                    onSelectMotor = { viewModel.equipMotor(it) },
                                    onSelectBattery = { viewModel.equipBattery(it) }
                                )
                                3 -> SkinSelector(
                                    current = equipment.skin,
                                    isUnlocked = { repo.isSkinUnlocked(it) },
                                    canAfford = { warPoints >= it.priceWP },
                                    onSelect = { viewModel.equipSkin(it) }
                                )
                            }
                        }

                        // Bottom Launch Sortie Bar
                        HangarLaunchBar(
                            onSingleplayer = { viewModel.startFlight(isMultiplayer = false) },
                            onMultiplayer = { viewModel.navigateTo(AppScreen.MULTIPLAYER) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HangarTopBar(
    callsign: String,
    warPoints: Int,
    tokens: Int,
    onNavigate: (AppScreen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Callsign & Title
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ПАНЦИРЬ: FPV",
                    color = TacticalOrange,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF263238), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = callsign,
                        color = Color(0xFF00E5FF),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Navigation Shortcuts
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Upgrades
            FilledTonalButton(
                onClick = { onNavigate(AppScreen.UPGRADES) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("nav_upgrades_button")
            ) {
                Icon(Icons.Default.Upgrade, contentDescription = null, tint = TacticalOrange, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("ПРОКАЧКА", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Bold)
            }

            // Challenges & Daily
            FilledTonalButton(
                onClick = { onNavigate(AppScreen.CHALLENGES) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("nav_challenges_button")
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TacticalAmber, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("ИСПЫТАНИЯ", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Bold)
            }

            // Leaderboard
            FilledTonalButton(
                onClick = { onNavigate(AppScreen.LEADERBOARD) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("nav_leaderboard_button")
            ) {
                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("ТОП ПИЛОТОВ", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(16.dp))

        // Currency Badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // War Points
            Box(
                modifier = Modifier
                    .background(Color(0xFF1B2A1C), RoundedCornerShape(6.dp))
                    .border(1.dp, RadarGreen, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("WP", color = RadarGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$warPoints",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Tokens
            Box(
                modifier = Modifier
                    .background(Color(0xFF382A14), RoundedCornerShape(6.dp))
                    .border(1.dp, TacticalAmber, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("★", color = TacticalAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$tokens",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DronePreviewPane(
    equipment: com.example.model.PlayerDroneEquipment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Hero Drone Art
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0C1118))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_pantsir_fpv_1792782566522),
                    contentDescription = "Дрон Камикадзе",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Current Skin Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color(0x99000000), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = equipment.skin.title,
                        color = Color(equipment.skin.hexColorAccent),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Drone Specifications Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SpecItem("МАССА", "${equipment.frame.massKg + equipment.warhead.massKg} кг")
                SpecItem("ТЯГА", "${equipment.frame.maxThrustN.toInt()} Н")
                SpecItem("ПРОБИТИЕ", "${equipment.warhead.penetrationMm} мм")
                SpecItem("АКБ", "${equipment.battery.capacityMah} mAh")
            }
        }
    }
}

@Composable
fun SpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun WarheadSelector(
    current: WarheadType,
    isUnlocked: (WarheadType) -> Boolean,
    canAfford: (WarheadType) -> Boolean,
    onSelect: (WarheadType) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(WarheadType.values()) { wh ->
            val unlocked = isUnlocked(wh)
            val selected = current == wh

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(wh) }
                    .testTag("warhead_item_${wh.name}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Color(0xFF263327) else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selected) RadarGreen else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wh.title,
                                color = if (selected) RadarGreen else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = wh.spec,
                                color = TacticalOrange,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "Эффективно против: ${wh.bonusDamageAgainst}",
                            color = TextDim,
                            fontSize = 11.sp
                        )
                    }

                    if (selected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = RadarGreen)
                    } else if (!unlocked) {
                        Button(
                            onClick = { onSelect(wh) },
                            enabled = canAfford(wh),
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${wh.priceWP} WP", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("ВЫБРАТЬ", color = TacticalOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FrameSelector(
    current: DroneFrameId,
    isUnlocked: (DroneFrameId) -> Boolean,
    canAfford: (DroneFrameId) -> Boolean,
    onSelect: (DroneFrameId) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DroneFrameId.values()) { fr ->
            val unlocked = isUnlocked(fr)
            val selected = current == fr

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(fr) }
                    .testTag("frame_item_${fr.name}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Color(0xFF263327) else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selected) RadarGreen else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fr.title,
                            color = if (selected) RadarGreen else TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = fr.description,
                            color = TextDim,
                            fontSize = 11.sp
                        )
                    }

                    if (selected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = RadarGreen)
                    } else if (!unlocked) {
                        Button(
                            onClick = { onSelect(fr) },
                            enabled = canAfford(fr),
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${fr.priceWP} WP", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("ВЫБРАТЬ", color = TacticalOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MotorBatterySelector(
    currentMotor: MotorTypeId,
    currentBattery: BatteryTypeId,
    onSelectMotor: (MotorTypeId) -> Unit,
    onSelectBattery: (BatteryTypeId) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("МОТОРЫ", color = TacticalOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        items(MotorTypeId.values()) { motor ->
            val selected = currentMotor == motor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectMotor(motor) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Color(0xFF263327) else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(motor.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("KV: ${motor.kv} | Множитель тяги: x${motor.thrustMultiplier}", color = TextDim, fontSize = 10.sp)
                    }
                    if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = RadarGreen)
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text("АККУМУЛЯТОРЫ (LiPo / Li-Ion)", color = TacticalOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        items(BatteryTypeId.values()) { batt ->
            val selected = currentBattery == batt
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectBattery(batt) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Color(0xFF263327) else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(batt.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Емкость: ${batt.capacityMah} mAh | Токоотдача: ${batt.cRate}C", color = TextDim, fontSize = 10.sp)
                    }
                    if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = RadarGreen)
                }
            }
        }
    }
}

@Composable
fun SkinSelector(
    current: SkinId,
    isUnlocked: (SkinId) -> Boolean,
    canAfford: (SkinId) -> Boolean,
    onSelect: (SkinId) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(SkinId.values()) { skin ->
            val unlocked = isUnlocked(skin)
            val selected = current == skin

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(skin) }
                    .testTag("skin_item_${skin.name}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Color(0xFF263327) else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selected) RadarGreen else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skin color circle preview
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(skin.hexColorPrimary), CircleShape)
                            .border(2.dp, Color(skin.hexColorAccent), CircleShape)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(skin.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(skin.previewBadge, color = TextDim, fontSize = 11.sp)
                    }

                    if (selected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = RadarGreen)
                    } else if (!unlocked) {
                        Button(
                            onClick = { onSelect(skin) },
                            enabled = canAfford(skin),
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("${skin.priceWP} WP", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("ПРИМЕНИТЬ", color = TacticalOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HangarLaunchBar(
    onSingleplayer: () -> Unit,
    onMultiplayer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Multiplayer Arena Launch Button
        Button(
            onClick = onMultiplayer,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("multiplayer_arena_button")
        ) {
            Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF00E5FF))
            Spacer(Modifier.width(6.dp))
            Text("СЕТЕВАЯ АРЕНА", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Singleplayer Sortie Launch Button
        Button(
            onClick = onSingleplayer,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
            modifier = Modifier
                .weight(1.3f)
                .height(44.dp)
                .testTag("launch_sortie_button")
        ) {
            Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("БОЕВОЙ ВЫЛЕТ (ПАНЦИРЬ)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

package com.example.ui.multiplayer

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HUDCyan
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalOrange
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextLight
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

data class BattleRoom(
    val id: String,
    val title: String,
    val mapName: String,
    val activePilots: Int,
    val maxPilots: Int,
    val pingMs: Int,
    val threatLevel: String,
    val objectives: String
)

@Composable
fun MultiplayerLobbyScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HANGAR)
    }

    val rooms = remember {
        listOf(
            BattleRoom(
                id = "room_1",
                title = "Операция «Черный Панцирь»",
                mapName = "Сектор «Западный Рубеж»",
                activePilots = 4,
                maxPilots = 6,
                pingMs = 22,
                threatLevel = "ВЫСОКАЯ (ЗРПК + РЭБ)",
                objectives = "Совместный штурм батареи Панцирь-С1 и танковой колонны"
            ),
            BattleRoom(
                id = "room_2",
                title = "Штурм Промзоны (Хардкор)",
                mapName = "Завод «Прогресс»",
                activePilots = 3,
                maxPilots = 4,
                pingMs = 31,
                threatLevel = "ЭКСТРЕМАЛЬНАЯ",
                objectives = "Уничтожение складов боеприпасов под плотным огнем 30-мм спарок"
            ),
            BattleRoom(
                id = "room_3",
                title = "Воздушный Перехват FPV",
                mapName = "Высота 144",
                activePilots = 5,
                maxPilots = 8,
                pingMs = 18,
                threatLevel = "СРЕДНЯЯ (FPV Dogfight)",
                objectives = "Свободная охота на дроны противника и радарные вышки"
            )
        )
    }

    var selectedRoomIndex by remember { mutableIntStateOf(0) }
    val currentRoom = rooms[selectedRoomIndex]

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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HANGAR) },
                    modifier = Modifier.testTag("back_button_multiplayer")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }

                Spacer(Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "СЕТЕВАЯ АРЕНА: СРАЖЕНИЯ ОНЛАЙН",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E3A24), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("СЕРВЕР: EU-CENTRAL (24ms)", color = RadarGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Кооперативные и соревновательные вылеты против тяжелой ПВО",
                        color = TextDim,
                        fontSize = 11.sp
                    )
                }
            }

            // Split View: Left = Room List, Right = Room Details & Launch
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left: Room Browser
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "АКТИВНЫЕ БОЕВЫЕ СЕССИИ",
                            color = TacticalOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(8.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(rooms.indices.toList()) { index ->
                                val room = rooms[index]
                                val selected = selectedRoomIndex == index

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRoomIndex = index }
                                        .testTag("room_item_$index"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) Color(0xFF1C2E3D) else DarkSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selected) HUDCyan else Color.Transparent
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = room.title,
                                                color = if (selected) HUDCyan else TextLight,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Text(
                                                text = "${room.activePilots}/${room.maxPilots} пилотов",
                                                color = TacticalAmber,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(room.mapName, color = TextDim, fontSize = 11.sp)
                                            Spacer(Modifier.weight(1f))
                                            Text("${room.pingMs} ms", color = RadarGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Right: Selected Room Briefing & Launch
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = currentRoom.title.uppercase(),
                            color = HUDCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Локация: ${currentRoom.mapName} | Угроза: ${currentRoom.threatLevel}",
                            color = TacticalOrange,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "БОЕВАЯ ЗАДАЧА:",
                            color = TextDim,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentRoom.objectives,
                            color = TextLight,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "ЗВЕНО ОПЕРАТОРОВ В СЕТИ:",
                            color = TextDim,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Connected pilots list
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val pilots = listOf(
                                Triple(viewModel.repository.callsign + " (ВЫ)", "Шмель-7", "ГОТОВ"),
                                Triple("Стриж_22", "Мамонт-10", "ГОТОВ"),
                                Triple("Viper_Pro", "Стрела-Х", "ГОТОВ"),
                                Triple("Valkyrie_UA", "Шмель-7", "В ПОЛЕТЕ")
                            )

                            items(pilots) { (call, frame, status) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = TacticalOrange, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(call, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(6.dp))
                                    Text("[$frame]", color = TextDim, fontSize = 10.sp)
                                    Spacer(Modifier.weight(1f))
                                    Text(status, color = RadarGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Launch button
                        Button(
                            onClick = {
                                viewModel.startFlight(isMultiplayer = true)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("join_multiplayer_battle_button")
                        ) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("ПОДКЛЮЧИТЬСЯ И ВЗЛЕТЕТЬ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

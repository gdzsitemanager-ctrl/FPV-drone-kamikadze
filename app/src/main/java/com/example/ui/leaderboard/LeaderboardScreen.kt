package com.example.ui.leaderboard

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
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.LeaderboardEntry
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

@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val leaderboard by viewModel.leaderboard.collectAsState()

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
                    modifier = Modifier.testTag("back_button_leaderboard")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = "ТАБЛИЦА ЛИДЕРОВ: ТОП ОПЕРАТОРОВ FPV",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Мировой рейтинг пилотов по уничтожению комплексов ПВО и боевой техники",
                        color = TextDim,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = TacticalAmber, modifier = Modifier.size(28.dp))
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", color = TextDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))
                Text("ПОЗЫВНОЙ ПИЛОТА", color = TextDim, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("ДИВИЗИОН", color = TextDim, fontSize = 11.sp, modifier = Modifier.width(100.dp))
                Text("УНИЧТОЖЕНО", color = TextDim, fontSize = 11.sp, modifier = Modifier.width(90.dp))
                Text("ТОЧНОСТЬ", color = TextDim, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                Text("РЕЙТИНГ ОЧКОВ", color = TextDim, fontSize = 11.sp, modifier = Modifier.width(110.dp))
            }

            // Leaderboard Entries
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(leaderboard) { entry ->
                    val isPlayer = entry.isCurrentPlayer

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("leaderboard_row_${entry.rank}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlayer) Color(0xFF263327) else DarkSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPlayer) RadarGreen else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        when (entry.rank) {
                                            1 -> TacticalAmber
                                            2 -> Color(0xFFC0C0C0)
                                            3 -> Color(0xFFCD7F32)
                                            else -> DarkSurfaceVariant
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${entry.rank}",
                                    color = if (entry.rank <= 3) Color.Black else TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            // Callsign
                            Text(
                                text = if (isPlayer) "${entry.callsign} (ВЫ)" else entry.callsign,
                                color = if (isPlayer) RadarGreen else TextLight,
                                fontWeight = if (isPlayer) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )

                            // Division
                            Text(
                                text = entry.division,
                                color = when (entry.division) {
                                    "Призрак" -> Color(0xFFE040FB)
                                    "Ас" -> TacticalOrange
                                    "Ветеран" -> HUDCyan
                                    else -> TextDim
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.width(100.dp)
                            )

                            // Kills
                            Text(
                                text = "${entry.kills} целей",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(90.dp)
                            )

                            // Accuracy
                            Text(
                                text = "${entry.accuracyPct}%",
                                color = RadarGreen,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(80.dp)
                            )

                            // Score
                            Text(
                                text = "${entry.score}",
                                color = TacticalOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

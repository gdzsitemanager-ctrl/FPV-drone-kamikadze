package com.example.ui.challenges

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
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
import com.example.model.DailyReward
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
fun ChallengesScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val dailyRewards by viewModel.dailyRewards.collectAsState()
    val weeklyChallenges by viewModel.weeklyChallenges.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val currentStreak = viewModel.repository.dailyStreak

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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HANGAR) },
                    modifier = Modifier.testTag("back_button_challenges")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = "ЕЖЕДНЕВНЫЕ НАГРАДЫ И ИСПЫТАНИЯ",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Серия входов 7 дней | Еженедельные боевые задачи | Достижения",
                        color = TextDim,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TacticalAmber, modifier = Modifier.size(26.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: 7-Day Login Streak
                item {
                    Text(
                        text = "СЕРИЯ ЕЖЕДНЕВНЫХ НАГРАД (ДЕНЬ $currentStreak ИЗ 7)",
                        color = TacticalOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(dailyRewards) { reward ->
                            val isToday = reward.day == currentStreak
                            val isClaimed = reward.isClaimed

                            Card(
                                modifier = Modifier
                                    .width(105.dp)
                                    .testTag("daily_card_day_${reward.day}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isToday -> Color(0xFF263327)
                                        isClaimed -> Color(0xFF141A1F)
                                        else -> DarkSurface
                                    }
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isToday) RadarGreen else DarkBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "ДЕНЬ ${reward.day}",
                                        color = if (isToday) RadarGreen else TextDim,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "+${reward.rewardWP} WP",
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "+${reward.rewardTokens} ★",
                                        color = TacticalAmber,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (reward.specialItem != null) {
                                        Text(
                                            text = reward.specialItem,
                                            color = HUDCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.height(6.dp))
                                    if (isClaimed) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RadarGreen, modifier = Modifier.size(18.dp))
                                    } else if (isToday) {
                                        Button(
                                            onClick = { viewModel.claimDailyReward(reward.day) },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = RadarGreen),
                                            modifier = Modifier
                                                .height(28.dp)
                                                .testTag("claim_daily_button")
                                        ) {
                                            Text("ЗАБРАТЬ", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text("ЗАКРЫТО", color = TextDim, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Weekly Challenges
                item {
                    Text(
                        text = "ЕЖЕНЕДЕЛЬНЫЕ БОЕВЫЕ ИСПЫТАНИЯ",
                        color = TacticalOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                items(weeklyChallenges) { challenge ->
                    val isDone = challenge.currentProgress >= challenge.targetProgress
                    val isClaimed = challenge.isCompleted

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("challenge_card_${challenge.id}"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(challenge.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${challenge.currentProgress} / ${challenge.targetProgress}",
                                        color = if (isDone) RadarGreen else TacticalAmber,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(challenge.description, color = TextDim, fontSize = 11.sp)
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (challenge.currentProgress.toFloat() / challenge.targetProgress).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(5.dp),
                                    color = if (isDone) RadarGreen else TacticalOrange,
                                    trackColor = Color(0xFF263238)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            if (isClaimed) {
                                Text("ПОЛУЧЕНО", color = RadarGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else if (isDone) {
                                Button(
                                    onClick = { viewModel.claimWeeklyChallenge(challenge.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RadarGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("claim_challenge_${challenge.id}")
                                ) {
                                    Text("+${challenge.rewardWP} WP", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("+${challenge.rewardWP} WP", color = TacticalOrange, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Section 3: Achievements
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "ДОСТИЖЕНИЯ ПИЛОТА",
                        color = TacticalOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                items(achievements) { ach ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("achievement_card_${ach.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ach.isUnlocked) Color(0xFF1E2E22) else DarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (ach.isUnlocked) RadarGreen else DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = if (ach.isUnlocked) RadarGreen else TacticalAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(ach.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(ach.description, color = TextDim, fontSize = 11.sp)
                            }

                            Spacer(Modifier.width(8.dp))

                            if (ach.isUnlocked) {
                                Text("ВЫПОЛНЕНО", color = RadarGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text(
                                    text = "${ach.currentProgress}/${ach.maxProgress}",
                                    color = TacticalAmber,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.MissionResult

@Composable
fun ImpactResultDialog(
    result: MissionResult,
    onRestartFlight: () -> Unit,
    onReturnHangar: () -> Unit
) {
    Dialog(onDismissRequest = onReturnHangar) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(2.dp, if (result.isSuccess) RadarGreen else Color(0xFFFF1744), RoundedCornerShape(16.dp))
                .testTag("mission_result_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.HighlightOff,
                        contentDescription = null,
                        tint = if (result.isSuccess) RadarGreen else Color(0xFFFF1744),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (result.isSuccess) "ЦЕЛЬ ПОРАЖЕНА!" else "ВЫЛЕТ ЗАВЕРШЕН",
                        color = if (result.isSuccess) RadarGreen else Color(0xFFFF1744),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Target Title
                Text(
                    text = result.targetHitTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                if (result.hitZone != null) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0x33FF6D00), RoundedCornerShape(6.dp))
                            .border(1.dp, TacticalOrange, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = result.hitZone.title,
                            color = TacticalOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161E28), RoundedCornerShape(10.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("УРОН", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "${result.damageDealt.toInt()} HP",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("НАГРАДА", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "+${result.wpEarned} WP",
                            color = RadarGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ОЧКИ", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "+${result.scoreEarned}",
                            color = TacticalOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = result.details,
                    color = Color(0xFFBAC7D5),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReturnHangar,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("return_hangar_button")
                    ) {
                        Text("В АНГАР", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRestartFlight,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("retry_sortie_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("СЛЕДУЮЩИЙ ВЫЛЕТ", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlayerProfileEntity
import com.example.model.WeatherType
import com.example.ui.theme.*

@Composable
fun RpgTopAppBar(
    profile: PlayerProfileEntity?,
    currentHp: Int,
    currentMana: Int,
    currentStamina: Int,
    weather: WeatherType,
    cloudStatus: String,
    onWeatherClick: () -> Unit,
    onCloudClick: () -> Unit,
    onCustomizeClick: () -> Unit
) {
    Surface(
        color = RpgDarkSurface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Row 1: Profile identity, currencies, weather, and actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Character info badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(RpgDarkSurfaceVariant)
                        .clickable { onCustomizeClick() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(profile?.auraColorHex ?: 0xFF00E5FF),
                                        RpgGoldPrimary
                                    )
                                )
                            )
                    ) {
                        Text(
                            text = "${profile?.level ?: 1}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = profile?.name ?: "Герой",
                            color = RpgTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "[${profile?.guildTag ?: "DAWN"}] ${profile?.characterClassName ?: "WARRIOR"}",
                            color = RpgGoldPrimary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Currencies & Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Gold
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RpgDarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("💰", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${profile?.gold ?: 0}",
                            color = RpgGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Crystals
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RpgDarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("💎", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${profile?.crystals ?: 0}",
                            color = RpgArcaneCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Weather Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF22293D))
                            .clickable { onWeatherClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(weather.iconEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = weather.title.take(6),
                            color = RpgTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Cloud Sync Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B382B))
                            .clickable { onCloudClick() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Облако",
                                tint = RpgEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Облако", color = RpgEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Vitals Gauges (HP, Mana, Stamina)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val maxHp = (profile?.maxHp ?: 250).coerceAtLeast(1)
                val maxMana = (profile?.maxMana ?: 70).coerceAtLeast(1)
                val maxStamina = (profile?.maxStamina ?: 100).coerceAtLeast(1)

                // HP Bar
                VitalGauge(
                    label = "HP $currentHp/$maxHp",
                    fraction = (currentHp.toFloat() / maxHp).coerceIn(0f, 1f),
                    barColor = RpgCrimson,
                    modifier = Modifier.weight(1f)
                )

                // Mana Bar
                VitalGauge(
                    label = "MP $currentMana/$maxMana",
                    fraction = (currentMana.toFloat() / maxMana).coerceIn(0f, 1f),
                    barColor = RpgArcaneCyan,
                    modifier = Modifier.weight(1f)
                )

                // Stamina Bar
                VitalGauge(
                    label = "STA $currentStamina/$maxStamina",
                    fraction = (currentStamina.toFloat() / maxStamina).coerceIn(0f, 1f),
                    barColor = RpgGoldPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VitalGauge(
    label: String,
    fraction: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = RpgTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF232838))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

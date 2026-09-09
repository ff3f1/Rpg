package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TerritoryRecordEntity
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun GuildAndTerritoryScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val territories by viewModel.territories.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RpgDarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Guild Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(RpgGoldPrimary, RpgCrimson)
                                    )
                                )
                        ) {
                            Text("🛡️", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile?.guildName ?: "Рыцари Рассвета",
                                    color = RpgTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RpgDarkSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("[${profile?.guildTag ?: "DAWN"}]", color = RpgGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("Уровень гильдии: ${profile?.guildLevel ?: 4} • Участников: 28/40", color = RpgTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("«Свет Первого Рассвета рассеет мглу Бездны!»", color = RpgGoldPrimary, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Guild Active Perks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GuildPerkChip("Бонус XP", "+15%", Modifier.weight(1f))
                        GuildPerkChip("Доход налогов", "+10%", Modifier.weight(1f))
                        GuildPerkChip("Урон осады", "+8%", Modifier.weight(1f))
                    }
                }
            }
        }

        // Territory Wars Headline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Карта Захвата Территорий", color = RpgTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Контролируйте стратегические регионы для сбора дани", color = RpgTextSecondary, fontSize = 10.sp)
                }
            }
        }

        // List of territories
        items(territories) { terr ->
            TerritoryItemCard(
                territory = terr,
                userGuild = profile?.guildName ?: "Рыцари Рассвета",
                onSiege = { viewModel.captureTerritory(terr.territoryId) }
            )
        }
    }
}

@Composable
private fun GuildPerkChip(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RpgDarkSurfaceVariant)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = RpgTextSecondary, fontSize = 9.sp)
            Text(value, color = RpgEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TerritoryItemCard(
    territory: TerritoryRecordEntity,
    userGuild: String,
    onSiege: () -> Unit
) {
    val isControlledByUs = territory.controllerGuild.equals(userGuild, ignoreCase = true)
    Card(
        colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isControlledByUs) "🏰" else "⚔️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(territory.name, color = RpgTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Регион: ${territory.region}", color = RpgTextSecondary, fontSize = 10.sp)
                    }
                }

                if (isControlledByUs) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B382B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Защищено вами", color = RpgEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onSiege,
                        colors = ButtonDefaults.buttonColors(containerColor = RpgCrimson),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Захватить", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Контроль: ${territory.controllerGuild}", color = if (isControlledByUs) RpgGoldPrimary else RpgTextSecondary, fontSize = 11.sp)
                Text("Сбор налогов: +${territory.taxYield} G/час", color = RpgGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

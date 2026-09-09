package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AchievementRecordEntity
import com.example.data.local.QuestRecordEntity
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun QuestsAndAchievementsScreen(viewModel: GameViewModel) {
    var selectedSection by remember { mutableStateOf("Квесты") }
    val quests by viewModel.quests.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val leaderboard = remember {
        listOf(
            Triple(1, "Архонт_Маэлгор [DAWN] • 💻 PC", "Боевая мощь: 84,200"),
            Triple(2, "Валькирия_Ингрид [VALK] • 📱 Android", "Боевая мощь: 79,500"),
            Triple(3, "Теневой_Клинок [VOID] • 🍏 iOS", "Боевая мощь: 75,100"),
            Triple(4, "Серафим_Света [DAWN] • 💻 PC", "Боевая мощь: 71,900"),
            Triple(5, "Повелитель_Грома [STORM] • 📱 Android", "Боевая мощь: 68,400")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RpgDarkBackground)
            .padding(14.dp)
    ) {
        // Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Квесты", "Достижения", "Зал Славы").forEach { tab ->
                val isSel = selectedSection == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) RpgGoldPrimary else RpgDarkSurfaceVariant)
                        .clickable { selectedSection = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSel) Color.Black else RpgTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION 1: Daily Quests
        if (selectedSection == "Квесты") {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Ежедневные поручения гильдии (Сброс через 8ч)", color = RpgTextSecondary, fontSize = 11.sp)
                }

                items(quests) { quest ->
                    QuestItemCard(quest = quest, onClaim = { viewModel.claimQuestReward(quest.questId) })
                }
            }
        }

        // SECTION 2: Achievements
        if (selectedSection == "Достижения") {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Трофеи и подвиги в Этерии", color = RpgTextSecondary, fontSize = 11.sp)
                }

                items(achievements) { ach ->
                    AchievementItemCard(achievement = ach, onClaim = { viewModel.claimAchievementReward(ach.achievementId) })
                }
            }
        }

        // SECTION 3: Hall of Fame
        if (selectedSection == "Зал Славы") {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Глобальный кроссплатформенный рейтинг (Сезон I)", color = RpgTextSecondary, fontSize = 11.sp)
                }

                items(leaderboard) { (rank, name, score) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (rank) {
                                            1 -> RpgGoldPrimary
                                            2 -> Color(0xFFC0C0C0)
                                            3 -> Color(0xFFCD7F32)
                                            else -> RpgDarkSurfaceVariant
                                        }
                                    )
                            ) {
                                Text(
                                    text = "$rank",
                                    color = if (rank <= 3) Color.Black else RpgTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, color = RpgTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(score, color = RpgGoldPrimary, fontSize = 10.sp)
                            }
                            Text("🏆", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestItemCard(quest: QuestRecordEntity, onClaim: () -> Unit) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(quest.title, color = RpgTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(quest.description, color = RpgTextSecondary, fontSize = 10.sp)
                }

                if (quest.isClaimed) {
                    Text("Забрано ✔", color = RpgEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else if (quest.isCompleted) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = RpgGoldPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Забрать", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("${quest.currentProgress}/${quest.targetProgress}", color = RpgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            val pct = (quest.currentProgress.toFloat() / quest.targetProgress).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF202636))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(pct)
                        .background(if (quest.isCompleted) RpgEmerald else RpgGoldPrimary)
                )
            }
        }
    }
}

@Composable
private fun AchievementItemCard(achievement: AchievementRecordEntity, onClaim: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (achievement.isUnlocked) RpgGoldPrimary else Color(0xFF37474F))
            ) {
                Text("🏆", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(achievement.title, color = RpgTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(achievement.description, color = RpgTextSecondary, fontSize = 10.sp)
                Text("Награда: +${achievement.rewardCrystals} Кристаллов Этерии", color = RpgArcaneCyan, fontSize = 9.sp)
            }

            if (achievement.isClaimed) {
                Text("Получено", color = RpgTextSecondary, fontSize = 10.sp)
            } else if (achievement.isUnlocked) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = RpgArcaneCyan),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Награда", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Заблокировано", color = Color.Gray, fontSize = 9.sp)
            }
        }
    }
}

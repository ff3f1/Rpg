package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NpcCharacter
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun StoryNpcDialogScreen(viewModel: GameViewModel) {
    val activeNpc by viewModel.activeNpc.collectAsState()
    val currentDialogueNodeId by viewModel.currentDialogueNodeId.collectAsState()
    val npcs by viewModel.npcs.collectAsState()
    val factionReputations by viewModel.factionReputations.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RpgDarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Factions Reputation Standing Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Репутация во Фракциях Этерии", color = RpgTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    factionReputations.forEach { (faction, rep) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(faction, color = RpgTextSecondary, fontSize = 11.sp)
                            val status = when {
                                rep >= 50 -> "Почитание ($rep)"
                                rep >= 20 -> "Дружелюбие ($rep)"
                                rep >= 0 -> "Нейтралитет ($rep)"
                                else -> "Враждебность ($rep)"
                            }
                            Text(status, color = if (rep >= 20) RpgEmerald else RpgGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active NPC Dialogue Box
        if (activeNpc != null) {
            val npc = activeNpc!!
            val node = npc.dialogues[currentDialogueNodeId] ?: npc.dialogues["start"]

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161C2C)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RpgGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(RpgPurple, RpgArcaneCyan)
                                            )
                                        )
                                ) {
                                    Text("🧙‍♂️", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(npc.name, color = RpgTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("${npc.role} • ${npc.faction}", color = RpgGoldPrimary, fontSize = 10.sp)
                                }
                            }

                            TextButton(onClick = { viewModel.closeDialogue() }) {
                                Text("Закрыть", color = RpgTextSecondary, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dialogue message text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(RpgDarkSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = node?.text ?: "",
                                color = RpgTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Branching choices
                        if (node != null && node.choices.isNotEmpty()) {
                            Text("Ваш ответ:", color = RpgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            node.choices.forEach { choice ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF242E47))
                                        .clickable { viewModel.selectDialogueChoice(choice) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text("› ${choice.text}", color = RpgGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Available NPCs List
        item {
            Text("Персонажи в мире Этерии (NPC)", color = RpgTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(npcs) { npc ->
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.startDialogueWithNpc(npc) }
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RpgDarkSurfaceVariant)
                    ) {
                        Text(if (npc.id.contains("seraphina")) "🔮" else "⚒️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(npc.name, color = RpgTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(npc.role, color = RpgGoldPrimary, fontSize = 10.sp)
                        Text("Локация: ${npc.location} • Отношение: ${npc.affinity}%", color = RpgTextSecondary, fontSize = 10.sp)
                    }
                    Button(
                        onClick = { viewModel.startDialogueWithNpc(npc) },
                        colors = ButtonDefaults.buttonColors(containerColor = RpgDarkSurfaceVariant),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Говорить", color = RpgArcaneCyan, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
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
import com.example.data.local.PlayerProfileEntity
import com.example.model.SkillNode
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun CharacterProgressionScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val skills by viewModel.skillTree.collectAsState()
    var selectedSkillBranch by remember { mutableStateOf("БОЙ") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RpgDarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header & 3D Avatar Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
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
                                Text("⚔️", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile?.name ?: "Герой",
                                    color = RpgTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Класс: ${profile?.characterClassName} • Уровень ${profile?.level}",
                                    color = RpgGoldPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openCustomization() },
                            colors = ButtonDefaults.buttonColors(containerColor = RpgDarkSurfaceVariant),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = RpgArcaneCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Облик", color = RpgArcaneCyan, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // XP Progress bar
                    val currentXp = profile?.xp ?: 0
                    val neededXp = (profile?.level ?: 1) * 250
                    val xpPct = (currentXp.toFloat() / neededXp).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Опыт персонажа", color = RpgTextSecondary, fontSize = 11.sp)
                        Text("$currentXp / $neededXp XP", color = RpgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2538))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(xpPct)
                                .background(Brush.horizontalGradient(listOf(RpgPurple, RpgArcaneCyan)))
                        )
                    }
                }
            }
        }

        // Attributes Allocation Block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Характеристики", color = RpgTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Свободных очков: ${profile?.unspentPoints ?: 0}",
                            color = if ((profile?.unspentPoints ?: 0) > 0) RpgGoldPrimary else RpgTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AttributeRow("Сила (STR)", "${profile?.strength}", "Увеличивает урон меча и физ. крит", canAdd = (profile?.unspentPoints ?: 0) > 0) {
                        viewModel.allocateAttribute("STR")
                    }
                    AttributeRow("Ловкость (AGI)", "${profile?.agility}", "Повышает скорость атаки и шанс уклонения", canAdd = (profile?.unspentPoints ?: 0) > 0) {
                        viewModel.allocateAttribute("AGI")
                    }
                    AttributeRow("Интеллект (INT)", "${profile?.intelligence}", "Увеличивает силу заклинаний и запас маны", canAdd = (profile?.unspentPoints ?: 0) > 0) {
                        viewModel.allocateAttribute("INT")
                    }
                    AttributeRow("Живучесть (VIT)", "${profile?.vitality}", "Увеличивает максимальное здоровье (+15 HP)", canAdd = (profile?.unspentPoints ?: 0) > 0) {
                        viewModel.allocateAttribute("VIT")
                    }
                }
            }
        }

        // Equipment Slots
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Экипированное Снаряжение", color = RpgTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    EquipmentSlotItem(
                        icon = "🗡️",
                        slotName = "Главная рука",
                        itemName = profile?.equippedWeapon ?: "Меч",
                        statDesc = "Урон оружия: +42 физ. атаки",
                        rarityColor = RpgCrimson
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EquipmentSlotItem(
                        icon = "🛡️",
                        slotName = "Доспех",
                        itemName = profile?.equippedArmor ?: "Латы",
                        statDesc = "Защита: +35 брони, +50 HP",
                        rarityColor = RpgArcaneCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EquipmentSlotItem(
                        icon = "💍",
                        slotName = "Кольцо стихий",
                        itemName = "Перстень Пламени и Льда",
                        statDesc = "Урон магии +15%, мана +30",
                        rarityColor = RpgPurple
                    )
                }
            }
        }

        // Skill Tree Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Древо Способностей", color = RpgTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Branch Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("БОЙ", "МАГИЯ", "ВЫЖИВАНИЕ").forEach { branch ->
                            val isSel = selectedSkillBranch == branch
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) RpgGoldPrimary else RpgDarkSurfaceVariant)
                                    .clickable { selectedSkillBranch = branch }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = branch,
                                    color = if (isSel) Color.Black else RpgTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredSkills = skills.filter { it.branch == selectedSkillBranch }
                    filteredSkills.forEach { skill ->
                        SkillItemRow(skill = skill, playerLevel = profile?.level ?: 1)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AttributeRow(
    title: String,
    value: String,
    desc: String,
    canAdd: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = RpgTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = RpgTextSecondary, fontSize = 10.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = RpgGoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onAdd,
                enabled = canAdd,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (canAdd) RpgGoldPrimary else Color(0xFF263238))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Повысить", tint = if (canAdd) Color.Black else Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EquipmentSlotItem(
    icon: String,
    slotName: String,
    itemName: String,
    statDesc: String,
    rarityColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RpgDarkSurfaceVariant)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(slotName, color = RpgTextSecondary, fontSize = 10.sp)
            }
            Text(itemName, color = rarityColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(statDesc, color = RpgEmerald, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SkillItemRow(skill: SkillNode, playerLevel: Int) {
    val isAvailable = playerLevel >= skill.requiredLevel
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RpgDarkSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (skill.isUnlocked) RpgGoldPrimary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (skill.isUnlocked) RpgGoldPrimary else Color(0xFF37474F))
        ) {
            Text(
                text = if (skill.branch == "МАГИЯ") "🔮" else if (skill.branch == "БОЙ") "⚔️" else "💚",
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(skill.name, color = RpgTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                if (skill.isUnlocked) {
                    Text("[Изучено]", color = RpgEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("[Требуется Ур. ${skill.requiredLevel}]", color = if (isAvailable) RpgGoldPrimary else Color.Gray, fontSize = 9.sp)
                }
            }
            Text(skill.description, color = RpgTextSecondary, fontSize = 10.sp)
            Text("Расход маны: ${skill.manaCost} MP • КД: ${skill.cooldownSeconds}с", color = RpgArcaneCyan, fontSize = 9.sp)
        }
    }
}

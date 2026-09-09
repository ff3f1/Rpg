package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CombatControlBar(
    onMeleeAttack: () -> Unit,
    onDash: () -> Unit,
    onSpellCast: (String) -> Unit,
    onHarvest: () -> Unit,
    onUsePotion: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
    ) {
        // Quick Utility Row: Potions and Harvest
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Harvest Button
            ActionRoundButton(
                emoji = "⛏️",
                label = "Сбор",
                bgBrush = Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))),
                size = 44,
                onClick = onHarvest
            )

            // HP Potion
            ActionRoundButton(
                emoji = "🧪",
                label = "HP",
                bgBrush = Brush.linearGradient(listOf(Color(0xFFC2185B), Color(0xFF880E4F))),
                size = 44,
                onClick = { onUsePotion("HP") }
            )

            // Mana Potion
            ActionRoundButton(
                emoji = "💧",
                label = "MP",
                bgBrush = Brush.linearGradient(listOf(Color(0xFF0288D1), Color(0xFF01579B))),
                size = 44,
                onClick = { onUsePotion("MANA") }
            )
        }

        // Spell Buttons Arc
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fireball
            ActionRoundButton(
                emoji = "🔥",
                label = "Огонь",
                bgBrush = Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFD84315))),
                size = 48,
                onClick = { onSpellCast("sk_fireball") }
            )

            // Frost Nova
            ActionRoundButton(
                emoji = "❄️",
                label = "Лед",
                bgBrush = Brush.linearGradient(listOf(Color(0xFF00B0FF), Color(0xFF0091EA))),
                size = 48,
                onClick = { onSpellCast("sk_frostnova") }
            )

            // Thunder Strike
            ActionRoundButton(
                emoji = "⚡",
                label = "Молния",
                bgBrush = Brush.linearGradient(listOf(Color(0xFFFFD600), Color(0xFFFFAB00))),
                size = 48,
                onClick = { onSpellCast("sk_thunder") }
            )

            // Healing Light
            ActionRoundButton(
                emoji = "✨",
                label = "Свет",
                bgBrush = Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00C853))),
                size = 48,
                onClick = { onSpellCast("sk_heal") }
            )
        }

        // Primary Combat Actions: Dash & Giant Attack Button
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dash / Dodge button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF455A64), Color(0xFF263238))
                        )
                    )
                    .clickable { onDash() }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💨", fontSize = 18.sp)
                    Text("Рывок", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Melee Attack Blade button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(RpgGoldPrimary, Color(0xFFFF6F00))
                        )
                    )
                    .clickable { onMeleeAttack() }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚔️", fontSize = 28.sp)
                    Text("Удар", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ActionRoundButton(
    emoji: String,
    label: String,
    bgBrush: Brush,
    size: Int,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgBrush)
            .clickable { onClick() }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = (size / 2.6f).sp)
            Text(label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

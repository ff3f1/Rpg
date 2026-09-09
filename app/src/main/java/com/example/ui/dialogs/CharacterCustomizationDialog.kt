package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.example.model.CharacterClass
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun CharacterCustomizationDialog(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()

    var heroName by remember(profile) { mutableStateOf(profile?.name ?: "Герой") }
    var selectedClass by remember(profile) { mutableStateOf(profile?.characterClassName ?: "WARRIOR") }
    var selectedHair by remember(profile) { mutableStateOf(profile?.hairStyle ?: "Воинский ирокез") }
    var selectedAuraHex by remember(profile) { mutableStateOf(profile?.auraColorHex ?: 0xFF00E5FF) }

    val auraColors = listOf(
        0xFF00E5FF to "Аркана",
        0xFFFFD700 to "Солнце",
        0xFFFF3366 to "Огонь",
        0xFF9D4EDD to "Тень",
        0xFF00E676 to "Природа"
    )

    val hairstyles = listOf("Воинский ирокез", "Косы севера", "Капюшон ассасина", "Шлем паладина")

    Dialog(onDismissRequest = { viewModel.closeCustomization() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = RpgDarkSurface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Кастомизация Персонажа",
                    color = RpgTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Name input
                OutlinedTextField(
                    value = heroName,
                    onValueChange = { heroName = it },
                    label = { Text("Имя героя") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RpgTextPrimary,
                        unfocusedTextColor = RpgTextPrimary,
                        focusedBorderColor = RpgGoldPrimary,
                        unfocusedBorderColor = RpgDarkSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Class selection
                Text("Класс персонажа:", color = RpgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CharacterClass.values().forEach { cls ->
                        val isSel = selectedClass == cls.name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) RpgGoldPrimary else RpgDarkSurfaceVariant)
                                .clickable { selectedClass = cls.name }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cls.displayName.take(4),
                                color = if (isSel) Color.Black else RpgTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hairstyle selection
                Text("Стиль прически / шлема:", color = RpgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    hairstyles.forEach { hair ->
                        val isSel = selectedHair == hair
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) RpgArcaneCyan else RpgDarkSurfaceVariant)
                                .clickable { selectedHair = hair }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = hair.take(6),
                                color = if (isSel) Color.Black else RpgTextPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aura glow selection
                Text("Цвет боевой ауры:", color = RpgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    auraColors.forEach { (colorHex, _) ->
                        val isSel = selectedAuraHex == colorHex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSel) 3.dp else 0.dp,
                                    color = if (isSel) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedAuraHex = colorHex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.closeCustomization() }) {
                        Text("Отмена", color = RpgTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateCustomization(heroName, selectedClass, selectedHair, selectedAuraHex)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RpgGoldPrimary)
                    ) {
                        Text("Сохранить", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

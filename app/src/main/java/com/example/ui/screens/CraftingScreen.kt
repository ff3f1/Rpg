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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InventoryEntity
import com.example.model.CraftingRecipe
import com.example.model.ItemRarity
import com.example.model.ItemType
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun CraftingScreen(viewModel: GameViewModel) {
    var selectedCategory by remember { mutableStateOf("Кузница") }
    val inventory by viewModel.inventory.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()

    val recipes = remember {
        listOf(
            CraftingRecipe(
                id = "cr_dragonblade",
                name = "Огненный клинок Дракона",
                category = "Кузница",
                resultItemName = "Огненный клинок Дракона",
                resultRarity = ItemRarity.EPIC,
                resultType = ItemType.WEAPON,
                requiredGold = 350,
                requiredMaterials = listOf("Магическая железная руда" to 6, "Ядро пламени" to 2),
                description = "Легендарный клинок, пылающий вечным жаром драконьих копей. +55 к атаке."
            ),
            CraftingRecipe(
                id = "cr_sunplate",
                name = "Латы Небесного Солнца",
                category = "Кузница",
                resultItemName = "Латы Небесного Солнца",
                resultRarity = ItemRarity.LEGENDARY,
                resultType = ItemType.ARMOR,
                requiredGold = 500,
                requiredMaterials = listOf("Магическая железная руда" to 10, "Лунная трава" to 4),
                description = "Тяжелый доспех из закаленного мифрила с благословением жрецов. +45 к защите."
            ),
            CraftingRecipe(
                id = "cr_healelixir",
                name = "Великий эликсир исцеления",
                category = "Алхимия",
                resultItemName = "Великое зелье здоровья",
                resultRarity = ItemRarity.RARE,
                resultType = ItemType.POTION,
                requiredGold = 80,
                requiredMaterials = listOf("Лунная трава" to 3),
                description = "Восстанавливает 150 единиц здоровья прямо в гуще сражения."
            ),
            CraftingRecipe(
                id = "cr_manaelixir",
                name = "Эликсир концентрации маны",
                category = "Алхимия",
                resultItemName = "Эликсир концентрации маны",
                resultRarity = ItemRarity.RARE,
                resultType = ItemType.POTION,
                requiredGold = 80,
                requiredMaterials = listOf("Лунная трава" to 3),
                description = "Восстанавливает 100 единиц магической энергии."
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RpgDarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Кузница", "Алхимия", "Зачарование", "Инвентарь").forEach { cat ->
                    val isSel = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) RpgGoldPrimary else RpgDarkSurfaceVariant)
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = if (isSel) Color.Black else RpgTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section: Enchanting / Refinement
        if (selectedCategory == "Зачарование") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Наковальня Зачарования", color = RpgTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Улучшайте текущее оружие рунами ярости. Каждая заточка дает +15% урона и визуальное свечение.",
                            color = RpgTextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(RpgDarkSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Экипировано: ${profile?.equippedWeapon}", color = RpgGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Требуется для усиления: 1х Руна ярости войны + 150 Золота", color = RpgArcaneCyan, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.enhanceEquippedWeapon() },
                            colors = ButtonDefaults.buttonColors(containerColor = RpgGoldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Улучшить оружие (+1)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Inventory List
        if (selectedCategory == "Инвентарь") {
            item {
                Text(
                    text = "Содержимое рюкзака (${inventory.size} предметов)",
                    color = RpgTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(inventory) { item ->
                InventoryItemCard(item)
            }
        }

        // Section: Recipes (Кузница & Алхимия)
        if (selectedCategory == "Кузница" || selectedCategory == "Алхимия") {
            item {
                Text(
                    text = "Доступные чертежи и рецепты",
                    color = RpgTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val catRecipes = recipes.filter { it.category == selectedCategory }
            items(catRecipes) { recipe ->
                RecipeCard(recipe = recipe, inventory = inventory, onCraft = { viewModel.craftItem(recipe) })
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: CraftingRecipe,
    inventory: List<InventoryEntity>,
    onCraft: () -> Unit
) {
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
                    Text(
                        text = if (recipe.category == "Кузница") "⚒️" else "🧪",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(recipe.name, color = RpgTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Категория: ${recipe.category} • Стоимость: ${recipe.requiredGold} G", color = RpgGoldPrimary, fontSize = 10.sp)
                    }
                }

                Button(
                    onClick = onCraft,
                    colors = ButtonDefaults.buttonColors(containerColor = RpgGoldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Сковать", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(recipe.description, color = RpgTextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))

            // Required materials list
            Text("Необходимые ресурсы:", color = RpgTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            recipe.requiredMaterials.forEach { req ->
                val have = inventory.find { it.name.contains(req.first, ignoreCase = true) }?.count ?: 0
                val enough = have >= req.second
                Text(
                    text = "• ${req.first}: $have / ${req.second} шт.",
                    color = if (enough) RpgEmerald else RpgCrimson,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun InventoryItemCard(item: InventoryEntity) {
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(RpgDarkSurfaceVariant)
            ) {
                Text(
                    text = when (item.type) {
                        "WEAPON" -> "🗡️"
                        "ARMOR" -> "🛡️"
                        "POTION" -> "🧪"
                        "MATERIAL" -> "💎"
                        "RUNE" -> "✨"
                        else -> "📦"
                    },
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, color = RpgTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("x${item.count}", color = RpgGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(item.description, color = RpgTextSecondary, fontSize = 10.sp)
                Text("Редкость: ${item.rarity} • Цена продажи: ${item.sellPrice} G", color = RpgArcaneCyan, fontSize = 9.sp)
            }
        }
    }
}

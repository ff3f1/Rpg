package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun MarketplaceAndSocialScreen(viewModel: GameViewModel) {
    var subTab by remember { mutableStateOf("Биржа") }
    val listings by viewModel.marketListings.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val party by viewModel.coopParty.collectAsState()

    var chatChannel by remember { mutableStateOf("ГИЛЬДИЯ") }
    var chatInputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RpgDarkBackground)
            .padding(14.dp)
    ) {
        // Sub-tab switcher: Биржа / Кооп / Чат
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Биржа", "Кооп Рейды", "Живой Чат").forEach { tab ->
                val isSel = subTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) RpgGoldPrimary else RpgDarkSurfaceVariant)
                        .clickable { subTab = tab }
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

        // TAB 1: Marketplace
        if (subTab == "Биржа") {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Межсерверная Биржа Этерии", color = RpgTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Торгуйте редкими ресурсами между Android, PC и iOS", color = RpgTextSecondary, fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    viewModel.postMarketListing("Зелье древней силы", ItemType.POTION, ItemRarity.EPIC, 2, 220)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RpgGoldPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("+ Продать", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                items(listings) { listing ->
                    MarketListingCard(listing = listing, onBuy = { viewModel.buyMarketListing(listing) })
                }
            }
        }

        // TAB 2: Co-op Lobby
        if (subTab == "Кооп Рейды") {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Кооп Лобби: Комната #AETH-884", color = RpgTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Подземелье: 'Логово Ледяного Дракона'", color = RpgArcaneCyan, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1B382B))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("4/4 Готовы", color = RpgEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            party.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (member.platform) {
                                                "PC" -> "💻"
                                                "iOS" -> "🍏"
                                                else -> "📱"
                                            },
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(member.name, color = RpgTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("${member.characterClass.displayName} • Ур. ${member.level}", color = RpgTextSecondary, fontSize = 10.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${member.pingMs}ms", color = RpgEmerald, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🟢", fontSize = 9.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.startCoopRaid() },
                                colors = ButtonDefaults.buttonColors(containerColor = RpgCrimson),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Начать совместный рейд в коопе", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: Real-Time Chat
        if (subTab == "Живой Чат") {
            Column(modifier = Modifier.weight(1f)) {
                // Channel selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ГИЛЬДИЯ", "МИР", "СИСТЕМА").forEach { ch ->
                        val isCh = chatChannel == ch
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCh) RpgGoldPrimary else RpgDarkSurfaceVariant)
                                .clickable { chatChannel = ch }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ch, color = if (isCh) Color.Black else RpgTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Messages Feed
                val filteredChat = chatMessages.filter {
                    if (chatChannel == "МИР") true else it.channel == chatChannel || it.channel == "ALL"
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredChat) { msg ->
                        ChatMessageBubble(msg = msg)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Send Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInputText,
                        onValueChange = { chatInputText = it },
                        placeholder = { Text("Сообщение в канал $chatChannel...", fontSize = 11.sp, color = RpgTextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RpgDarkSurface,
                            unfocusedContainerColor = RpgDarkSurface,
                            focusedBorderColor = RpgGoldPrimary,
                            unfocusedBorderColor = RpgDarkSurfaceVariant,
                            focusedTextColor = RpgTextPrimary,
                            unfocusedTextColor = RpgTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (chatInputText.isNotBlank()) {
                                viewModel.sendChatMessage(chatInputText, chatChannel)
                                chatInputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RpgGoldPrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Отправить", tint = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketListingCard(listing: MarketListing, onBuy: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RpgDarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (listing.sellerPlatform) {
                            "PC" -> "💻"
                            "iOS" -> "🍏"
                            else -> "📱"
                        },
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(listing.itemName, color = Color(listing.rarity.colorHex), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("x${listing.count}", color = RpgTextSecondary, fontSize = 11.sp)
                }
                Text("Продавец: ${listing.sellerName} • Осталось: ${listing.timeLeftMinutes} мин", color = RpgTextSecondary, fontSize = 9.sp)
            }

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = RpgGoldPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("${listing.priceGold} G", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(msg: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RpgDarkSurface)
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (msg.senderPlatform) {
                            "PC" -> "💻"
                            "iOS" -> "🍏"
                            else -> "📱"
                        },
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(msg.senderName, color = if (msg.isGuildOfficer) RpgGoldPrimary else RpgArcaneCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RpgDarkSurfaceVariant)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(msg.channel, color = RpgTextSecondary, fontSize = 8.sp)
                    }
                }
                Text(msg.timestamp, color = RpgTextSecondary, fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(msg.text, color = RpgTextPrimary, fontSize = 11.sp)
        }
    }
}

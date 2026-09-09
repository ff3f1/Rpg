package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Sync
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
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun CloudSyncDialog(viewModel: GameViewModel) {
    val syncStatus by viewModel.cloudSyncState.collectAsState()
    val syncCode by viewModel.cloudSyncCode.collectAsState()
    var inputToken by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { viewModel.closeCloudDialog() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = RpgDarkSurface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = RpgEmerald, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Кроссплатформенное Облако",
                        color = RpgTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ваш прогресс сохраняется в защищенном облачном хранилище и доступен на Android, iOS и PC.",
                    color = RpgTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current Device Cloud Code
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RpgDarkSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Ваш уникальный код синхронизации:", color = RpgTextSecondary, fontSize = 10.sp)
                        Text(syncCode, color = RpgGoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Статус: $syncStatus", color = RpgEmerald, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.triggerCloudSync() },
                    colors = ButtonDefaults.buttonColors(containerColor = RpgEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Синхронизировать сейчас", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = RpgDarkSurfaceVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // Restore from another device
                Text("Перенести сохранение с другого устройства:", color = RpgTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputToken,
                        onValueChange = { inputToken = it },
                        placeholder = { Text("Например: AETH-SYNC-...", fontSize = 11.sp, color = RpgTextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = RpgTextPrimary,
                            unfocusedTextColor = RpgTextPrimary,
                            focusedBorderColor = RpgGoldPrimary,
                            unfocusedBorderColor = RpgDarkSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputToken.isNotBlank()) {
                                viewModel.restoreCloudSave(inputToken.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RpgGoldPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("Импорт", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.closeCloudDialog() }) {
                        Text("Закрыть", color = RpgTextSecondary)
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ActionCombatEngine
import com.example.engine.EnemyType
import com.example.model.WeatherType
import com.example.ui.components.CombatControlBar
import com.example.ui.components.VirtualJoystick
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OpenWorldGameScreen(viewModel: GameViewModel) {
    val combatEngine = viewModel.combatEngine
    val weather by viewModel.currentWeather.collectAsState()
    val playerProfile by viewModel.playerProfile.collectAsState()
    val liveHp by viewModel.liveHp.collectAsState()

    var joystickInput by remember { mutableStateOf(Offset.Zero) }

    // Fast-tick recomposition trigger for smooth 60fps canvas rendering
    var renderTick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.008f, 0.033f)
            lastTime = now

            combatEngine.update(
                dt = dt,
                joystickX = joystickInput.x,
                joystickY = joystickInput.y,
                playerHp = liveHp
            )
            renderTick++
            kotlinx.coroutines.delay(16) // ~60fps
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RpgDarkBackground)
    ) {
        // Main 2D Action RPG Canvas with camera offset
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Camera centered on player
            val camX = (canvasW / 2f) - combatEngine.playerPos.x
            val camY = (canvasH / 2f) - combatEngine.playerPos.y

            // Draw World Ground Grid & Tiles
            val tileSize = 80f
            val startGridX = ((combatEngine.playerPos.x - canvasW / 2f) / tileSize).toInt() * tileSize
            val startGridY = ((combatEngine.playerPos.y - canvasH / 2f) / tileSize).toInt() * tileSize

            var gx = startGridX - tileSize
            while (gx < startGridX + canvasW + tileSize * 2) {
                var gy = startGridY - tileSize
                while (gy < startGridY + canvasH + tileSize * 2) {
                    val screenX = gx + camX
                    val screenY = gy + camY
                    val tileHash = ((gx.toInt() * 73856093) xor (gy.toInt() * 19349663)).hashCode()
                    val isStone = (tileHash % 5 == 0)
                    val baseTileColor = if (isStone) Color(0xFF141926) else Color(0xFF0F131D)

                    drawRect(
                        color = baseTileColor,
                        topLeft = Offset(screenX, screenY),
                        size = Size(tileSize - 2f, tileSize - 2f)
                    )
                    gy += tileSize
                }
                gx += tileSize
            }

            // Draw Mystic Runic Circles in the world
            val circleCenters = listOf(Offset(700f, 700f), Offset(450f, 450f), Offset(950f, 950f))
            for (c in circleCenters) {
                val sc = c + Offset(camX, camY)
                drawCircle(color = Color(0x1500E5FF), radius = 120f, center = sc)
                drawCircle(color = Color(0x33FFD700), radius = 120f, center = sc, style = Stroke(width = 2f))
                drawCircle(color = Color(0x2200E5FF), radius = 60f, center = sc, style = Stroke(width = 1.5f))
            }

            // Draw Resource Nodes (Mining veins, Moon herbs, Chests)
            for (node in combatEngine.resourceNodes) {
                if (node.harvestsLeft <= 0) continue
                val sp = node.position + Offset(camX, camY)
                when (node.resourceType) {
                    "IRON" -> {
                        drawCircle(color = Color(0xFF78909C), radius = 18f, center = sp)
                        drawCircle(color = Color(0xFF00E5FF), radius = 8f, center = sp)
                    }
                    "HERB" -> {
                        drawCircle(color = Color(0xFF2E7D32), radius = 16f, center = sp)
                        drawCircle(color = Color(0xFFB388FF), radius = 7f, center = sp)
                    }
                    "CHEST" -> {
                        drawRect(
                            color = Color(0xFFFFB300),
                            topLeft = sp - Offset(15f, 12f),
                            size = Size(30f, 24f)
                        )
                        drawRect(
                            color = Color(0xFF5D4037),
                            topLeft = sp - Offset(13f, 10f),
                            size = Size(26f, 20f)
                        )
                    }
                }
            }

            // Draw Ground Loot drops
            for (loot in combatEngine.groundLoots) {
                val lp = loot.position + Offset(camX, camY)
                drawCircle(color = Color(0x44FFD700), radius = 14f, center = lp)
                drawCircle(color = RpgGoldPrimary, radius = 7f, center = lp)
            }

            // Draw Enemies
            for (enemy in combatEngine.enemies) {
                if (enemy.isDead) continue
                val ep = enemy.position + Offset(camX, camY)
                val color = when {
                    enemy.isFrozen -> Color(0xFF00E5FF)
                    enemy.isBurning -> Color(0xFFFF5722)
                    else -> Color(enemy.type.bodyColor)
                }

                // Enemy body
                drawCircle(color = color, radius = enemy.type.radius, center = ep)
                drawCircle(color = Color.Black.copy(alpha = 0.5f), radius = enemy.type.radius, center = ep, style = Stroke(width = 2.5f))

                // Enemy health bar
                val barW = enemy.type.radius * 2.2f
                val hpPct = (enemy.currentHp.toFloat() / enemy.type.maxHp).coerceIn(0f, 1f)
                drawRect(
                    color = Color(0xFF212121),
                    topLeft = ep - Offset(barW / 2f, enemy.type.radius + 14f),
                    size = Size(barW, 5f)
                )
                drawRect(
                    color = if (enemy.type == EnemyType.ABYSSAL_OVERLORD) Color(0xFFFF1744) else Color(0xFFFF5252),
                    topLeft = ep - Offset(barW / 2f, enemy.type.radius + 14f),
                    size = Size(barW * hpPct, 5f)
                )
            }

            // Draw Companion (Archmage Seraphina)
            if (combatEngine.hasCompanion) {
                val compScreen = combatEngine.companionPos + Offset(camX, camY)
                drawCircle(color = Color(0x3300E5FF), radius = 22f, center = compScreen)
                drawCircle(color = Color(0xFF7E57C2), radius = 16f, center = compScreen)
                drawCircle(color = Color(0xFF00E5FF), radius = 6f, center = compScreen)
            }

            // Draw Player Character
            val pp = combatEngine.playerPos + Offset(camX, camY)
            val auraColor = Color(playerProfile?.auraColorHex ?: 0xFF00E5FF)

            // Aura ring
            drawCircle(color = auraColor.copy(alpha = 0.25f), radius = 28f, center = pp)
            drawCircle(color = auraColor.copy(alpha = 0.6f), radius = 22f, center = pp, style = Stroke(width = 1.5f))

            // Player Body
            drawCircle(color = Color(0xFF263238), radius = 19f, center = pp)
            drawCircle(color = RpgGoldPrimary, radius = 15f, center = pp)

            // Weapon swing animation & facing indicator
            val swordAngle = Math.toDegrees(combatEngine.playerFacingAngle.toDouble()).toFloat()
            rotate(degrees = swordAngle, pivot = pp) {
                // Sword blade
                drawLine(
                    color = if (combatEngine.meleeAttackAnim > 0f) Color.White else Color(0xFFE0E0E0),
                    start = pp + Offset(15f, 0f),
                    end = pp + Offset(38f + combatEngine.meleeAttackAnim * 12f, 0f),
                    strokeWidth = if (combatEngine.meleeAttackAnim > 0f) 5f else 3f
                )
                // Sword glow
                drawLine(
                    color = auraColor,
                    start = pp + Offset(18f, 0f),
                    end = pp + Offset(36f, 0f),
                    strokeWidth = 1.5f
                )
            }

            // Draw Projectiles
            for (proj in combatEngine.projectiles) {
                val projPos = proj.position + Offset(camX, camY)
                drawCircle(color = proj.type.color.copy(alpha = 0.4f), radius = proj.type.radius * 1.5f, center = projPos)
                drawCircle(color = proj.type.color, radius = proj.type.radius, center = projPos)
                drawCircle(color = Color.White, radius = proj.type.radius * 0.45f, center = projPos)
            }

            // Draw Particles
            for (p in combatEngine.particles) {
                val pPos = p.position + Offset(camX, camY)
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = pPos
                )
            }

            // Dynamic Weather Overlay
            when (weather) {
                WeatherType.RAIN_STORM -> {
                    // Rain dark tint
                    drawRect(color = Color(0x33102035), size = size)
                    if (combatEngine.lightningFlashAlpha > 0f) {
                        drawRect(color = Color.White.copy(alpha = combatEngine.lightningFlashAlpha * 0.6f), size = size)
                    }
                }
                WeatherType.FOGGY_DUSK -> {
                    drawRect(color = Color(0x442A1E3C), size = size)
                }
                WeatherType.BLOOD_MOON -> {
                    drawRect(color = Color(0x33550015), size = size)
                }
                WeatherType.CLEAR_DAY -> {
                    // Golden sun shimmer
                    drawRect(color = Color(0x08FFD700), size = size)
                }
            }
        }

        // Floating Damage & Loot Texts
        for (txt in combatEngine.floatingTexts) {
            val camX = (sizeDpToPx(360) / 2f)
            Box(
                modifier = Modifier
                    .offset(x = (txt.position.x - 700f + 160f).dp, y = (txt.position.y - 700f + 260f).dp)
            ) {
                Text(
                    text = txt.text,
                    color = txt.color.copy(alpha = txt.alpha),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        // Top Info Badge: Current Location & Boss tracker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC131722))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Шепчущий Лес [Ур. 1-5]", color = RpgTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Свободное исследование мира", color = RpgTextSecondary, fontSize = 9.sp)
                    }
                }
            }

            // Fast actions
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF880E4F))
                        .clickable {
                            combatEngine.resetWorld()
                            viewModel.showAlert("Монстры и руда в локации возродились!")
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("🔄 Респавн", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF006064))
                        .clickable {
                            combatEngine.playerPos = Offset(700f, 1100f)
                            viewModel.showAlert("Телепортация к Владыке Разлома!")
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("⚡ К Боссу", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Boss Health bar (when boss is alive and nearby)
        val boss = combatEngine.enemies.find { it.type == EnemyType.ABYSSAL_OVERLORD && !it.isDead }
        if (boss != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xDD1F0B13))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Владыка Разлома: ${boss.currentHp}/${EnemyType.ABYSSAL_OVERLORD.maxHp} HP",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF37000B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((boss.currentHp.toFloat() / EnemyType.ABYSSAL_OVERLORD.maxHp).coerceIn(0f, 1f))
                                .background(Color(0xFFFF1744))
                        )
                    }
                }
            }
        }

        // Bottom Controls: Virtual Joystick on Left, Combat Actions on Right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp, start = 12.dp, end = 6.dp)
        ) {
            // Joystick
            VirtualJoystick(
                modifier = Modifier.align(Alignment.BottomStart),
                sizeDp = 120,
                onMove = { x, y ->
                    joystickInput = Offset(x, y)
                }
            )

            // Combat Action Bar
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                CombatControlBar(
                    onMeleeAttack = { viewModel.performMeleeAttack() },
                    onDash = { viewModel.performDash() },
                    onSpellCast = { spellId -> viewModel.castSpell(spellId) },
                    onHarvest = { viewModel.harvestNearby() },
                    onUsePotion = { type -> viewModel.usePotion(type) }
                )
            }
        }
    }
}

private fun sizeDpToPx(dp: Int): Float = dp * 2.5f

package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.model.ItemRarity
import com.example.model.WeatherType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.*
import kotlin.random.Random

class ActionCombatEngine {
    // Arena boundaries
    val arenaWidth = 1400f
    val arenaHeight = 1400f

    // Player state
    var playerPos = Offset(700f, 700f)
    var playerFacingAngle = 0f // in radians
    var isDashing = false
    var dashCooldown = 0f
    var meleeAttackAnim = 0f // 0f to 1f for blade swing arc
    var companionPos = Offset(660f, 720f)
    var hasCompanion = true

    // Dynamic Weather
    var currentWeather: WeatherType = WeatherType.CLEAR_DAY
    var weatherTick = 0f
    var lightningFlashAlpha = 0f

    // Combat lists
    val enemies = mutableListOf<CombatEnemy>()
    val projectiles = mutableListOf<CombatProjectile>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()
    val groundLoots = mutableListOf<GroundLoot>()
    val resourceNodes = mutableListOf<ResourceNode>()

    // Notification callback for game updates (XP, Gold, Item drop, Quests)
    var onPlayerGainedXp: ((Int) -> Unit)? = null
    var onPlayerGainedGold: ((Int) -> Unit)? = null
    var onPlayerTookDamage: ((Int) -> Unit)? = null
    var onEnemyKilled: ((EnemyType) -> Unit)? = null
    var onResourceGathered: ((String) -> Unit)? = null

    init {
        resetWorld()
    }

    fun resetWorld() {
        enemies.clear()
        projectiles.clear()
        particles.clear()
        floatingTexts.clear()
        groundLoots.clear()
        resourceNodes.clear()

        playerPos = Offset(700f, 700f)
        companionPos = Offset(660f, 720f)

        // Spawn starter enemies in world
        enemies.add(CombatEnemy("e1", EnemyType.FOREST_GOBLIN, Offset(500f, 500f), EnemyType.FOREST_GOBLIN.maxHp))
        enemies.add(CombatEnemy("e2", EnemyType.FOREST_GOBLIN, Offset(900f, 520f), EnemyType.FOREST_GOBLIN.maxHp))
        enemies.add(CombatEnemy("e3", EnemyType.SKELETON_WARRIOR, Offset(420f, 850f), EnemyType.SKELETON_WARRIOR.maxHp))
        enemies.add(CombatEnemy("e4", EnemyType.SKELETON_WARRIOR, Offset(980f, 860f), EnemyType.SKELETON_WARRIOR.maxHp))
        enemies.add(CombatEnemy("e5", EnemyType.SHADOW_KNIGHT, Offset(700f, 320f), EnemyType.SHADOW_KNIGHT.maxHp))
        enemies.add(CombatEnemy("e6", EnemyType.FROST_DRAKE, Offset(1100f, 1100f), EnemyType.FROST_DRAKE.maxHp))
        enemies.add(CombatEnemy("boss1", EnemyType.ABYSSAL_OVERLORD, Offset(700f, 1200f), EnemyType.ABYSSAL_OVERLORD.maxHp))

        // Spawn harvestable nodes
        resourceNodes.add(ResourceNode("n1", "Жила мифриловой руды", Offset(550f, 620f), 3, "IRON"))
        resourceNodes.add(ResourceNode("n2", "Куст Лунной Травы", Offset(820f, 600f), 3, "HERB"))
        resourceNodes.add(ResourceNode("n3", "Древний рунический сундук", Offset(700f, 420f), 1, "CHEST"))
        resourceNodes.add(ResourceNode("n4", "Жила железной руды", Offset(350f, 750f), 3, "IRON"))
        resourceNodes.add(ResourceNode("n5", "Алтарь Звездной Пыли", Offset(1050f, 750f), 2, "HERB"))
    }

    fun update(dt: Float, joystickX: Float, joystickY: Float, playerHp: Int) {
        // Handle dash cooldown & movement
        if (dashCooldown > 0f) {
            dashCooldown = (dashCooldown - dt).coerceAtLeast(0f)
        }
        if (isDashing && dashCooldown < 0.6f) {
            isDashing = false
        }

        val moveSpeed = if (isDashing) 420f else 180f
        if (abs(joystickX) > 0.1f || abs(joystickY) > 0.1f) {
            playerFacingAngle = atan2(joystickY, joystickX)
            val nx = (playerPos.x + joystickX * moveSpeed * dt).coerceIn(40f, arenaWidth - 40f)
            val ny = (playerPos.y + joystickY * moveSpeed * dt).coerceIn(40f, arenaHeight - 40f)
            playerPos = Offset(nx, ny)
        }

        // Companion follows player
        val compTarget = playerPos + Offset(-cos(playerFacingAngle) * 50f, -sin(playerFacingAngle) * 50f)
        companionPos = Offset(
            companionPos.x + (compTarget.x - companionPos.x) * 4f * dt,
            companionPos.y + (compTarget.y - companionPos.y) * 4f * dt
        )

        // Attack animation decay
        if (meleeAttackAnim > 0f) {
            meleeAttackAnim = (meleeAttackAnim - dt * 3.5f).coerceAtLeast(0f)
        }

        // Weather effects tick
        weatherTick += dt
        if (currentWeather == WeatherType.RAIN_STORM) {
            if (Random.nextFloat() < 0.04f) {
                lightningFlashAlpha = 0.8f
            }
            // Add falling rain particles
            for (i in 0..2) {
                val rx = playerPos.x + Random.nextFloat() * 600f - 300f
                val ry = playerPos.y + Random.nextFloat() * 600f - 300f
                particles.add(
                    Particle(
                        position = Offset(rx, ry),
                        velocity = Offset(-20f, 320f),
                        color = Color(0x88A0C0FF),
                        radius = 2.5f,
                        lifeTime = 0.4f
                    )
                )
            }
        }
        if (lightningFlashAlpha > 0f) {
            lightningFlashAlpha = (lightningFlashAlpha - dt * 2f).coerceAtLeast(0f)
        }

        // Update Projectiles
        val projIterator = projectiles.iterator()
        while (projIterator.hasNext()) {
            val proj = projIterator.next()
            proj.position += proj.velocity * dt
            proj.lifeTime -= dt

            // Trail particles
            particles.add(
                Particle(
                    position = proj.position,
                    velocity = Offset(Random.nextFloat() * 20f - 10f, Random.nextFloat() * 20f - 10f),
                    color = proj.type.color,
                    radius = 4f,
                    lifeTime = 0.3f
                )
            )

            // Check collision with enemies
            if (proj.fromPlayer) {
                for (enemy in enemies) {
                    if (!enemy.isDead && (enemy.position - proj.position).getDistance() < enemy.type.radius + proj.type.radius) {
                        applyDamageToEnemy(enemy, proj.type.damage, proj.type.isMagic, proj.type)
                        proj.lifeTime = 0f
                        break
                    }
                }
            }

            if (proj.lifeTime <= 0f) {
                // Impact particle burst
                for (i in 0..6) {
                    particles.add(
                        Particle(
                            position = proj.position,
                            velocity = Offset(Random.nextFloat() * 120f - 60f, Random.nextFloat() * 120f - 60f),
                            color = proj.type.color,
                            radius = 3.5f,
                            lifeTime = 0.4f
                        )
                    )
                }
                projIterator.remove()
            }
        }

        // Update Enemies
        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            if (enemy.isDead) continue

            // Freeze timer
            if (enemy.isFrozen) {
                enemy.freezeTimer -= dt
                if (enemy.freezeTimer <= 0f) enemy.isFrozen = false
            }

            // Burn timer
            if (enemy.isBurning) {
                enemy.burnTimer -= dt
                if (Random.nextFloat() < 0.15f) {
                    applyDamageToEnemy(enemy, 15, true, null)
                }
                if (enemy.burnTimer <= 0f) enemy.isBurning = false
            }

            // Attack cooldown
            if (enemy.attackCooldown > 0f) {
                enemy.attackCooldown -= dt
            }

            // Enemy movement towards player if not frozen
            if (!enemy.isFrozen) {
                val distToPlayer = (playerPos - enemy.position).getDistance()
                if (distToPlayer < 450f && distToPlayer > 30f) {
                    val dir = (playerPos - enemy.position) / distToPlayer
                    enemy.position += dir * (enemy.type.speed * 45f * dt)
                }

                // Attack player
                if (distToPlayer <= 50f && enemy.attackCooldown <= 0f && !isDashing) {
                    enemy.attackCooldown = 1.6f
                    val damage = enemy.type.attackPower
                    onPlayerTookDamage?.invoke(damage)
                    floatingTexts.add(
                        FloatingText(
                            UUID.randomUUID().toString(),
                            "-$damage",
                            playerPos + Offset(Random.nextFloat() * 20f - 10f, -30f),
                            Color(0xFFFF3333)
                        )
                    )
                }
            }
        }

        // Companion AI: Shoots arcane beam at nearest enemy periodically
        if (hasCompanion && Random.nextFloat() < 0.035f) {
            val nearest = enemies.filter { !it.isDead && (it.position - companionPos).getDistance() < 320f }
                .minByOrNull { (it.position - companionPos).getDistance() }
            if (nearest != null) {
                val dir = (nearest.position - companionPos)
                val len = dir.getDistance()
                if (len > 0f) {
                    projectiles.add(
                        CombatProjectile(
                            id = UUID.randomUUID().toString(),
                            position = companionPos,
                            velocity = (dir / len) * 450f,
                            type = ProjectileType.FROST_SHARD,
                            fromPlayer = true
                        )
                    )
                }
            }
        }

        // Update Particles
        val partIterator = particles.iterator()
        while (partIterator.hasNext()) {
            val p = partIterator.next()
            p.position += p.velocity * dt
            p.lifeTime -= dt
            p.alpha = (p.lifeTime / 0.5f).coerceIn(0f, 1f)
            if (p.lifeTime <= 0f) {
                partIterator.remove()
            }
        }

        // Update Floating Texts
        val textIterator = floatingTexts.iterator()
        while (textIterator.hasNext()) {
            val t = textIterator.next()
            t.position = Offset(t.position.x, t.position.y - 35f * dt)
            t.lifeTime -= dt
            t.alpha = (t.lifeTime / 1.0f).coerceIn(0f, 1f)
            if (t.lifeTime <= 0f) {
                textIterator.remove()
            }
        }

        // Auto pickup ground loot near player
        val lootIterator = groundLoots.iterator()
        while (lootIterator.hasNext()) {
            val loot = lootIterator.next()
            if ((playerPos - loot.position).getDistance() < 60f) {
                if (loot.goldAmount > 0) {
                    onPlayerGainedGold?.invoke(loot.goldAmount)
                    floatingTexts.add(
                        FloatingText(
                            UUID.randomUUID().toString(),
                            "+${loot.goldAmount} Золота",
                            playerPos + Offset(0f, -40f),
                            Color(0xFFFFD700)
                        )
                    )
                }
                lootIterator.remove()
            }
        }
    }

    fun performMeleeAttack(strengthBonus: Int, weaponBonus: Int) {
        meleeAttackAnim = 1.0f
        val baseDamage = 35 + strengthBonus * 2 + weaponBonus
        val attackOrigin = playerPos + Offset(cos(playerFacingAngle) * 35f, sin(playerFacingAngle) * 35f)

        // Spawn blade slash particles in an arc
        for (i in -3..3) {
            val angle = playerFacingAngle + i * 0.2f
            val px = playerPos.x + cos(angle) * 55f
            val py = playerPos.y + sin(angle) * 55f
            particles.add(
                Particle(
                    position = Offset(px, py),
                    velocity = Offset(cos(angle) * 80f, sin(angle) * 80f),
                    color = Color(0xFFFFD700),
                    radius = 4f,
                    lifeTime = 0.25f
                )
            )
        }

        // Damage enemies in front arc
        for (enemy in enemies) {
            if (enemy.isDead) continue
            val dist = (enemy.position - attackOrigin).getDistance()
            if (dist < 75f) {
                val isCrit = (Random.nextInt(100) < (15 + currentWeather.critBonusPercent))
                val totalDamage = if (isCrit) (baseDamage * 1.8f).toInt() else baseDamage
                applyDamageToEnemy(enemy, totalDamage, isMagic = false, projectileType = null, isCrit = isCrit)
            }
        }
    }

    fun castSpell(spellId: String, intelligenceBonus: Int, manaAvailable: Int): Boolean {
        when (spellId) {
            "sk_fireball" -> {
                if (manaAvailable < 20) return false
                val base = (75 + intelligenceBonus * 3) * currentWeather.fireMultiplier
                val dir = Offset(cos(playerFacingAngle), sin(playerFacingAngle))
                projectiles.add(
                    CombatProjectile(
                        id = UUID.randomUUID().toString(),
                        position = playerPos + dir * 30f,
                        velocity = dir * 480f,
                        type = ProjectileType.FIREBALL,
                        fromPlayer = true
                    )
                )
                return true
            }
            "sk_frostnova" -> {
                if (manaAvailable < 25) return false
                val base = (55 + intelligenceBonus * 2) * currentWeather.waterFrostMultiplier
                // Freeze all enemies within 160 radius
                for (enemy in enemies) {
                    if (enemy.isDead) continue
                    if ((enemy.position - playerPos).getDistance() < 160f) {
                        enemy.isFrozen = true
                        enemy.freezeTimer = 3.5f
                        applyDamageToEnemy(enemy, base.toInt(), isMagic = true, projectileType = ProjectileType.FROST_SHARD)
                    }
                }
                // Ice wave particles
                for (i in 0..24) {
                    val angle = (i.toFloat() / 24f) * 2f * Math.PI.toFloat()
                    particles.add(
                        Particle(
                            position = playerPos,
                            velocity = Offset(cos(angle) * 160f, sin(angle) * 160f),
                            color = Color(0xFF00E5FF),
                            radius = 4.5f,
                            lifeTime = 0.45f
                        )
                    )
                }
                return true
            }
            "sk_thunder" -> {
                if (manaAvailable < 30) return false
                val base = (120 + intelligenceBonus * 4) * currentWeather.lightningMultiplier
                // Strike nearest enemy
                val target = enemies.filter { !it.isDead && (it.position - playerPos).getDistance() < 350f }
                    .minByOrNull { (it.position - playerPos).getDistance() }
                val targetPos = target?.position ?: (playerPos + Offset(cos(playerFacingAngle) * 120f, sin(playerFacingAngle) * 120f))

                if (target != null) {
                    applyDamageToEnemy(target, base.toInt(), isMagic = true, projectileType = ProjectileType.THUNDER_BOLT)
                }

                // Lightning line visual particles
                for (step in 0..12) {
                    val p = Offset(targetPos.x + Random.nextFloat() * 20f - 10f, targetPos.y - (12 - step) * 25f)
                    particles.add(
                        Particle(
                            position = p,
                            velocity = Offset(0f, 15f),
                            color = Color(0xFFFFEB3B),
                            radius = 5f,
                            lifeTime = 0.35f
                        )
                    )
                }
                return true
            }
            "sk_heal" -> {
                if (manaAvailable < 35) return false
                // Golden healing aura particles
                for (i in 0..16) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val dist = Random.nextFloat() * 40f
                    particles.add(
                        Particle(
                            position = playerPos + Offset(cos(angle) * dist, sin(angle) * dist),
                            velocity = Offset(0f, -40f),
                            color = Color(0xFF00E676),
                            radius = 4f,
                            lifeTime = 0.6f
                        )
                    )
                }
                return true
            }
        }
        return false
    }

    fun performDash(staminaAvailable: Int): Boolean {
        if (staminaAvailable < 25 || dashCooldown > 0f) return false
        isDashing = true
        dashCooldown = 0.9f
        // Dash ghost particles
        for (i in 0..6) {
            particles.add(
                Particle(
                    position = playerPos + Offset(Random.nextFloat() * 20f - 10f, Random.nextFloat() * 20f - 10f),
                    velocity = Offset(0f, 0f),
                    color = Color(0x66FFFFFF),
                    radius = 8f,
                    lifeTime = 0.3f
                )
            )
        }
        return true
    }

    fun harvestNearbyNode(): ResourceNode? {
        val node = resourceNodes.find { (it.position - playerPos).getDistance() < 70f && it.harvestsLeft > 0 }
        if (node != null) {
            node.harvestsLeft--
            onResourceGathered?.invoke(node.resourceType)
            floatingTexts.add(
                FloatingText(
                    UUID.randomUUID().toString(),
                    "+Ресурсы [${node.name}]",
                    playerPos + Offset(0f, -40f),
                    Color(0xFF81C784)
                )
            )
            // Harvesting spark particles
            for (i in 0..8) {
                particles.add(
                    Particle(
                        position = node.position,
                        velocity = Offset(Random.nextFloat() * 60f - 30f, Random.nextFloat() * -50f - 20f),
                        color = Color(0xFF4CAF50),
                        radius = 3.5f,
                        lifeTime = 0.5f
                    )
                )
            }
        }
        return node
    }

    private fun applyDamageToEnemy(
        enemy: CombatEnemy,
        damage: Int,
        isMagic: Boolean,
        projectileType: ProjectileType?,
        isCrit: Boolean = false
    ) {
        enemy.currentHp -= damage
        val displayColor = when {
            isCrit -> Color(0xFFFFD700)
            isMagic -> Color(0xFF00E5FF)
            else -> Color(0xFFFF5252)
        }
        val prefix = if (isCrit) "КРИТ! " else ""
        floatingTexts.add(
            FloatingText(
                UUID.randomUUID().toString(),
                "$prefix-$damage",
                enemy.position + Offset(Random.nextFloat() * 20f - 10f, -25f),
                displayColor
            )
        )

        if (enemy.currentHp <= 0) {
            enemy.isDead = true
            onEnemyKilled?.invoke(enemy.type)
            onPlayerGainedXp?.invoke(enemy.type.xpYield)
            onPlayerGainedGold?.invoke(enemy.type.goldYield)

            floatingTexts.add(
                FloatingText(
                    UUID.randomUUID().toString(),
                    "+${enemy.type.xpYield} XP",
                    enemy.position + Offset(0f, -45f),
                    Color(0xFFBA68C8)
                )
            )

            // Spawn ground loot
            groundLoots.add(
                GroundLoot(
                    id = UUID.randomUUID().toString(),
                    name = "Золотые монеты",
                    position = enemy.position,
                    goldAmount = enemy.type.goldYield,
                    rarity = if (enemy.type == EnemyType.ABYSSAL_OVERLORD) ItemRarity.MYTHIC else ItemRarity.RARE
                )
            )

            // Death burst particles
            for (i in 0..16) {
                particles.add(
                    Particle(
                        position = enemy.position,
                        velocity = Offset(Random.nextFloat() * 140f - 70f, Random.nextFloat() * 140f - 70f),
                        color = Color(enemy.type.bodyColor),
                        radius = 5f,
                        lifeTime = 0.6f
                    )
                )
            }
        }
    }
}

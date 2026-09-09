package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.model.ItemRarity

enum class EnemyType(
    val title: String,
    val maxHp: Int,
    val attackPower: Int,
    val speed: Float,
    val xpYield: Int,
    val goldYield: Int,
    val bodyColor: Long,
    val radius: Float
) {
    SKELETON_WARRIOR("Скелет-воин", 120, 15, 2.2f, 35, 25, 0xFFECEFF1, 24f),
    FOREST_GOBLIN("Лесной гоблин", 90, 12, 3.2f, 25, 18, 0xFF4CAF50, 20f),
    SHADOW_KNIGHT("Рыцарь Бездны", 260, 32, 2.0f, 90, 75, 0xFF3F51B5, 30f),
    FROST_DRAKE("Морозный дракончик", 380, 40, 2.6f, 150, 120, 0xFF00E5FF, 36f),
    ABYSSAL_OVERLORD("Владыка Разлома [БОСС]", 950, 65, 1.8f, 500, 450, 0xFFFF1744, 46f)
}

data class CombatEnemy(
    val id: String,
    val type: EnemyType,
    var position: Offset,
    var currentHp: Int,
    var isFrozen: Boolean = false,
    var freezeTimer: Float = 0f,
    var isBurning: Boolean = false,
    var burnTimer: Float = 0f,
    var attackCooldown: Float = 0f,
    var isDead: Boolean = false
)

enum class ProjectileType(val damage: Int, val isMagic: Boolean, val color: Color, val radius: Float) {
    FIREBALL(70, true, Color(0xFFFF5722), 16f),
    FROST_SHARD(45, true, Color(0xFF00E5FF), 12f),
    THUNDER_BOLT(110, true, Color(0xFFFFEB3B), 14f),
    MELEE_SLASH(55, false, Color(0xFFFFD700), 28f)
}

data class CombatProjectile(
    val id: String,
    var position: Offset,
    val velocity: Offset,
    val type: ProjectileType,
    val fromPlayer: Boolean,
    var lifeTime: Float = 2.0f
)

data class Particle(
    var position: Offset,
    var velocity: Offset,
    var color: Color,
    var radius: Float,
    var alpha: Float = 1.0f,
    var lifeTime: Float = 1.0f
)

data class FloatingText(
    val id: String,
    val text: String,
    var position: Offset,
    val color: Color,
    var alpha: Float = 1.0f,
    var lifeTime: Float = 1.2f
)

data class GroundLoot(
    val id: String,
    val name: String,
    val position: Offset,
    val goldAmount: Int = 0,
    val rarity: ItemRarity = ItemRarity.COMMON
)

data class ResourceNode(
    val id: String,
    val name: String,
    val position: Offset,
    var harvestsLeft: Int = 3,
    val resourceType: String // "IRON", "HERB", "CHEST"
)

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Герой Этерии",
    val characterClassName: String = "WARRIOR",
    val level: Int = 1,
    val xp: Int = 0,
    val hp: Int = 250,
    val maxHp: Int = 250,
    val mana: Int = 60,
    val maxMana: Int = 60,
    val stamina: Int = 100,
    val maxStamina: Int = 100,
    val strength: Int = 16,
    val agility: Int = 10,
    val intelligence: Int = 6,
    val vitality: Int = 18,
    val unspentPoints: Int = 5,
    val gold: Int = 450,
    val crystals: Int = 25,
    val equippedWeapon: String = "Железный полуторный меч",
    val equippedArmor: String = "Кованая кольчуга",
    val hairStyle: String = "Короткие воинские",
    val hairColorHex: Long = 0xFF4E342E,
    val auraColorHex: Long = 0xFF00E5FF,
    val gender: String = "Мужской",
    val guildName: String = "Рыцари Рассвета",
    val guildTag: String = "DAWN",
    val guildLevel: Int = 3,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory_items")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: String,
    val name: String,
    val type: String,
    val count: Int,
    val rarity: String,
    val description: String,
    val sellPrice: Int,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val magicBonus: Int = 0,
    val enhancementLevel: Int = 0
)

@Entity(tableName = "quest_records")
data class QuestRecordEntity(
    @PrimaryKey val questId: String,
    val title: String = "Ежедневный квест",
    val description: String = "Выполните боевое задание гильдии",
    val targetProgress: Int = 5,
    val currentProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)

@Entity(tableName = "achievement_records")
data class AchievementRecordEntity(
    @PrimaryKey val achievementId: String,
    val title: String = "Достижение Этерии",
    val description: String = "Совершите подвиг в мире",
    val rewardCrystals: Int = 20,
    val currentProgress: Int,
    val isUnlocked: Boolean,
    val isClaimed: Boolean
)

@Entity(tableName = "territory_records")
data class TerritoryRecordEntity(
    @PrimaryKey val territoryId: String,
    val name: String,
    val region: String,
    val controllerGuild: String,
    val defenseScore: Int,
    val taxYield: Int
)

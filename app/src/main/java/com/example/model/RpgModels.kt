package com.example.model

enum class CharacterClass(
    val displayName: String,
    val description: String,
    val baseHp: Int,
    val baseMana: Int,
    val baseStrength: Int,
    val baseAgility: Int,
    val baseIntelligence: Int,
    val baseVitality: Int,
    val primaryColorHex: Long
) {
    WARRIOR("Воин", "Мастер тяжелого оружия и сокрушительных ударов ближнего боя", 250, 60, 16, 10, 6, 18, 0xFFE53935),
    MAGE("Боевой Маг", "Повелитель стихийного огня, льда и молний", 160, 240, 6, 8, 20, 10, 0xFF00E5FF),
    ROGUE("Теневой Клинок", "Смертоносный убийца с парными кинжалами и высоким шансом крита", 190, 100, 11, 19, 9, 12, 0xFF9C27B0),
    PALADIN("Храмовник", "Несокрушимый защитник Света, исцеляющий союзников и сокрушающий нежить", 280, 140, 14, 8, 12, 20, 0xFFFFD700)
}

enum class WeatherType(
    val title: String,
    val description: String,
    val magicBonusDesc: String,
    val fireMultiplier: Float,
    val waterFrostMultiplier: Float,
    val lightningMultiplier: Float,
    val critBonusPercent: Int,
    val ambientColor: Long,
    val iconEmoji: String
) {
    CLEAR_DAY("Ясный день", "Солнце освещает земли Этерии", "Урон стихии Огня увеличен на 20%", 1.20f, 1.0f, 1.0f, 0, 0x00000000, "☀️"),
    RAIN_STORM("Грозовой шторм", "Проливной ливень с молниями", "Молнии и Лед получают +30% урона", 0.85f, 1.30f, 1.30f, 5, 0x33102040, "⛈️"),
    FOGGY_DUSK("Туманные сумерки", "Густой мистический туман снижает видимость", "Скрытность и шанс крита +20%", 1.0f, 1.10f, 1.0f, 20, 0x44201830, "🌫️"),
    BLOOD_MOON("Кровавая луна", "Темные силы пробуждаются. Монстры злее, награды выше!", "Урон всей магии +25%, монстры дают +50% опыта", 1.25f, 1.25f, 1.25f, 15, 0x44550010, "🌕")
}

enum class ItemRarity(val label: String, val colorHex: Long) {
    COMMON("Обычный", 0xFF9E9E9E),
    RARE("Редкий", 0xFF2196F3),
    EPIC("Эпический", 0xFF9C27B0),
    LEGENDARY("Легендарный", 0xFFFF9800),
    MYTHIC("Мифический", 0xFFFF1744)
}

enum class ItemType {
    WEAPON, ARMOR, POTION, MATERIAL, RUNE
}

data class EquipmentItem(
    val id: String,
    val name: String,
    val slot: String, // "WEAPON", "ARMOR", "RING", "AMULET"
    val rarity: ItemRarity,
    val attackBonus: Int,
    val defenseBonus: Int,
    val magicBonus: Int,
    val enhancementLevel: Int = 0,
    val description: String
)

data class InventoryItem(
    val id: String,
    val name: String,
    val type: ItemType,
    var count: Int,
    val rarity: ItemRarity,
    val description: String,
    val sellPrice: Int,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val magicBonus: Int = 0,
    val enhancementLevel: Int = 0
)

data class SkillNode(
    val id: String,
    val name: String,
    val branch: String, // "БОЙ", "МАГИЯ", "ВЫЖИВАНИЕ"
    val description: String,
    val requiredLevel: Int,
    val manaCost: Int,
    val cooldownSeconds: Float,
    val isUnlocked: Boolean = false,
    val iconName: String
)

data class CraftingRecipe(
    val id: String,
    val name: String,
    val category: String, // "Кузница", "Алхимия", "Зачарование"
    val resultItemName: String,
    val resultRarity: ItemRarity,
    val resultType: ItemType,
    val requiredGold: Int,
    val requiredMaterials: List<Pair<String, Int>>, // ItemName to quantity
    val description: String
)

data class DialogueChoice(
    val text: String,
    val nextNodeId: String,
    val factionDelta: Pair<String, Int>? = null, // Faction name to reputation points
    val goldReward: Int = 0,
    val xpReward: Int = 0,
    val triggerQuestId: String? = null
)

data class DialogueNode(
    val id: String,
    val speakerName: String,
    val speakerRole: String,
    val text: String,
    val choices: List<DialogueChoice>
)

data class NpcCharacter(
    val id: String,
    val name: String,
    val role: String,
    val faction: String,
    val location: String,
    val affinity: Int,
    val dialogues: Map<String, DialogueNode>,
    val canRecruitAsCompanion: Boolean = false,
    val isCompanionRecruited: Boolean = false
)

data class DailyQuest(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val goldReward: Int,
    val xpReward: Int,
    val crystalReward: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val isUnlocked: Boolean,
    val isClaimed: Boolean,
    val crystalReward: Int,
    val tier: String // "Бронза", "Серебро", "Золото", "Мифик"
)

data class Territory(
    val id: String,
    val name: String,
    val region: String,
    var controllerGuild: String,
    var taxYieldRatePerHour: Int,
    var defenseScore: Int,
    var isContested: Boolean = false
)

data class MarketListing(
    val id: String,
    val sellerName: String,
    val sellerPlatform: String, // "Android", "PC", "iOS"
    val itemName: String,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val count: Int,
    val priceGold: Int,
    val isPlayerListing: Boolean = false,
    val timeLeftMinutes: Int
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderPlatform: String,
    val channel: String, // "ГИЛЬДИЯ", "МИР", "РЕЙД", "СИСТЕМА"
    val text: String,
    val timestamp: String,
    val isGuildOfficer: Boolean = false
)

data class CoopPlayer(
    val name: String,
    val characterClass: CharacterClass,
    val platform: String,
    val level: Int,
    val pingMs: Int,
    val isHost: Boolean,
    val currentHpPercent: Float = 1.0f
)

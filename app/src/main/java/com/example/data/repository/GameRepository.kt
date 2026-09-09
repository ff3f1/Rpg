package com.example.data.repository

import com.example.data.local.*
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GameRepository(private val db: AppDatabase) {
    private val dao = db.appDao()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Current dynamic weather
    private val _currentWeather = MutableStateFlow(WeatherType.CLEAR_DAY)
    val currentWeather: StateFlow<WeatherType> = _currentWeather.asStateFlow()

    // Cloud Sync State
    private val _cloudSyncState = MutableStateFlow("Синхронизировано")
    val cloudSyncState: StateFlow<String> = _cloudSyncState.asStateFlow()

    private val _cloudSyncCode = MutableStateFlow("AETH-CLOUD-" + UUID.randomUUID().toString().take(8).uppercase())
    val cloudSyncCode: StateFlow<String> = _cloudSyncCode.asStateFlow()

    // Faction Reputations
    private val _factionReputations = MutableStateFlow(
        mapOf(
            "Королевская Стража" to 45,
            "Ковен Арканы" to 60,
            "Теневой Синдикат" to 15
        )
    )
    val factionReputations: StateFlow<Map<String, Int>> = _factionReputations.asStateFlow()

    // Live Market Listings
    private val _marketListings = MutableStateFlow(defaultMarketListings())
    val marketListings: StateFlow<List<MarketListing>> = _marketListings.asStateFlow()

    // Live Real-Time Chat Messages
    private val _chatMessages = MutableStateFlow(defaultChatMessages())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Co-op Room
    private val _coopParty = MutableStateFlow(defaultCoopParty())
    val coopParty: StateFlow<List<CoopPlayer>> = _coopParty.asStateFlow()

    // Skills Tree
    private val _skillTree = MutableStateFlow(defaultSkillNodes())
    val skillTree: StateFlow<List<SkillNode>> = _skillTree.asStateFlow()

    // NPC Roster
    private val _npcs = MutableStateFlow(defaultNpcCharacters())
    val npcs: StateFlow<List<NpcCharacter>> = _npcs.asStateFlow()

    // Room Database flows
    val playerProfileFlow: Flow<PlayerProfileEntity?> = dao.getPlayerProfileFlow()
    val inventoryFlow: Flow<List<InventoryEntity>> = dao.getAllInventoryItems()
    val questsFlow: Flow<List<QuestRecordEntity>> = dao.getAllQuests()
    val achievementsFlow: Flow<List<AchievementRecordEntity>> = dao.getAllAchievements()
    val territoriesFlow: Flow<List<TerritoryRecordEntity>> = dao.getAllTerritories()

    init {
        repositoryScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val existingProfile = dao.getPlayerProfile()
        if (existingProfile == null) {
            dao.insertOrUpdateProfile(
                PlayerProfileEntity(
                    id = 1,
                    name = "Валидор",
                    characterClassName = "WARRIOR",
                    level = 3,
                    xp = 340,
                    hp = 270,
                    maxHp = 270,
                    mana = 70,
                    maxMana = 70,
                    stamina = 100,
                    maxStamina = 100,
                    strength = 18,
                    agility = 12,
                    intelligence = 8,
                    vitality = 20,
                    unspentPoints = 4,
                    gold = 750,
                    crystals = 35,
                    equippedWeapon = "Огненный двуручник дракона",
                    equippedArmor = "Латы Серебряного Рассвета",
                    hairStyle = "Воинская коса",
                    auraColorHex = 0xFF00E5FF,
                    guildName = "Рыцари Рассвета",
                    guildTag = "DAWN",
                    guildLevel = 4
                )
            )

            // Seed inventory
            val starterInventory = listOf(
                InventoryEntity(itemId = "w_dragonsword", name = "Огненный двуручник дракона", type = "WEAPON", count = 1, rarity = "EPIC", description = "Закален в дыхании лавового дракона. +42 атаки.", sellPrice = 280, attackBonus = 42),
                InventoryEntity(itemId = "a_dawnplate", name = "Латы Серебряного Рассвета", type = "ARMOR", count = 1, rarity = "EPIC", description = "Освященные латы ордена. +35 защиты.", sellPrice = 240, defenseBonus = 35),
                InventoryEntity(itemId = "m_ironore", name = "Магическая железная руда", type = "MATERIAL", count = 14, rarity = "COMMON", description = "Основной металл для ковки брони и клинков.", sellPrice = 12),
                InventoryEntity(itemId = "m_moonherb", name = "Лунная трава", type = "MATERIAL", count = 8, rarity = "RARE", description = "Редкий алхимический цветок, распускающийся в сумерках.", sellPrice = 25),
                InventoryEntity(itemId = "m_firecore", name = "Ядро пламени", type = "MATERIAL", count = 3, rarity = "EPIC", description = "Пылающая эссенция элементалей огня.", sellPrice = 75),
                InventoryEntity(itemId = "p_hpelixir", name = "Великое зелье здоровья", type = "POTION", count = 5, rarity = "RARE", description = "Мгновенно восполняет 150 ед. здоровья.", sellPrice = 30),
                InventoryEntity(itemId = "p_manaelixir", name = "Эликсир концентрации маны", type = "POTION", count = 4, rarity = "RARE", description = "Мгновенно восстанавливает 100 ед. маны.", sellPrice = 30),
                InventoryEntity(itemId = "r_attackrune", name = "Руна ярости войны", type = "RUNE", count = 2, rarity = "LEGENDARY", description = "Вплавляется в оружие для улучшения до +1..+10.", sellPrice = 150)
            )
            dao.insertAllInventoryItems(starterInventory)

            // Seed daily quests
            dao.insertOrUpdateQuest(
                QuestRecordEntity(
                    questId = "q_skeletons",
                    title = "Истребление нежити",
                    description = "Уничтожьте 5 скелетов или гоблинов в Шепчущем Лесу",
                    targetProgress = 5,
                    currentProgress = 2,
                    isCompleted = false,
                    isClaimed = false
                )
            )
            dao.insertOrUpdateQuest(
                QuestRecordEntity(
                    questId = "q_crafting",
                    title = "Мастер наковальни",
                    description = "Сковать новое эпическое оружие в Кузнице",
                    targetProgress = 1,
                    currentProgress = 0,
                    isCompleted = false,
                    isClaimed = false
                )
            )
            dao.insertOrUpdateQuest(
                QuestRecordEntity(
                    questId = "q_alchemy",
                    title = "Тайны травника",
                    description = "Собрать лунную траву и сварить зелье здоровья",
                    targetProgress = 2,
                    currentProgress = 1,
                    isCompleted = false,
                    isClaimed = false
                )
            )
            dao.insertOrUpdateQuest(
                QuestRecordEntity(
                    questId = "q_market",
                    title = "Купеческий оборот",
                    description = "Продать или купить предмет на межсерверной бирже",
                    targetProgress = 1,
                    currentProgress = 1,
                    isCompleted = true,
                    isClaimed = false
                )
            )
            dao.insertOrUpdateQuest(
                QuestRecordEntity(
                    questId = "q_territory",
                    title = "Знамя рассвета",
                    description = "Захватить стратегическую крепость для своей гильдии",
                    targetProgress = 1,
                    currentProgress = 0,
                    isCompleted = false,
                    isClaimed = false
                )
            )

            // Seed achievements
            dao.insertOrUpdateAchievement(
                AchievementRecordEntity(
                    achievementId = "a_firstblood",
                    title = "Первая кровь",
                    description = "Одержите первую победу над монстрами открытого мира",
                    rewardCrystals = 25,
                    currentProgress = 1,
                    isUnlocked = true,
                    isClaimed = true
                )
            )
            dao.insertOrUpdateAchievement(
                AchievementRecordEntity(
                    achievementId = "a_blacksmith",
                    title = "Кузнец легенд",
                    description = "Создайте предмет эпического или легендарного качества",
                    rewardCrystals = 50,
                    currentProgress = 2,
                    isUnlocked = false,
                    isClaimed = false
                )
            )
            dao.insertOrUpdateAchievement(
                AchievementRecordEntity(
                    achievementId = "a_spellmaster",
                    title = "Верховный чародей",
                    description = "Примените 25 магических заклинаний в бою",
                    rewardCrystals = 35,
                    currentProgress = 12,
                    isUnlocked = false,
                    isClaimed = false
                )
            )
            dao.insertOrUpdateAchievement(
                AchievementRecordEntity(
                    achievementId = "a_conquest",
                    title = "Завоеватель Этерии",
                    description = "Захватите цитадель в войне гильдий",
                    rewardCrystals = 100,
                    currentProgress = 1,
                    isUnlocked = true,
                    isClaimed = false
                )
            )
            dao.insertOrUpdateAchievement(
                AchievementRecordEntity(
                    achievementId = "a_coopking",
                    title = "Братство оружия",
                    description = "Пройдите кооперативный рейд вместе с гильдией",
                    rewardCrystals = 75,
                    currentProgress = 1,
                    isUnlocked = true,
                    isClaimed = false
                )
            )

            // Seed territories
            val starterTerritories = listOf(
                TerritoryRecordEntity("t_woods", "Шепчущий Лес", "Западные земли", "Рыцари Рассвета", 850, 240),
                TerritoryRecordEntity("t_dragon", "Пик Дракона", "Северный хребет", "Пепельный Легион", 620, 480),
                TerritoryRecordEntity("t_citadel", "Затонувшая Цитадель", "Южные болота", "Ковен Арканы", 730, 360),
                TerritoryRecordEntity("t_harbor", "Золотая Гавань", "Восточное побережье", "Рыцари Рассвета", 920, 520),
                TerritoryRecordEntity("t_abyss", "Разлом Бездны", "Сердце тьмы", "Теневой Синдикат", 510, 650)
            )
            dao.insertAllTerritories(starterTerritories)
        }
    }

    suspend fun savePlayerProfile(profile: PlayerProfileEntity) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun addInventoryItem(item: InventoryEntity) {
        dao.insertInventoryItem(item)
    }

    suspend fun removeInventoryItem(id: Long) {
        dao.deleteInventoryItem(id)
    }

    suspend fun updateQuest(quest: QuestRecordEntity) {
        dao.insertOrUpdateQuest(quest)
    }

    suspend fun updateAchievement(achievement: AchievementRecordEntity) {
        dao.insertOrUpdateAchievement(achievement)
    }

    fun cycleWeather() {
        val nextWeather = when (_currentWeather.value) {
            WeatherType.CLEAR_DAY -> WeatherType.RAIN_STORM
            WeatherType.RAIN_STORM -> WeatherType.FOGGY_DUSK
            WeatherType.FOGGY_DUSK -> WeatherType.BLOOD_MOON
            WeatherType.BLOOD_MOON -> WeatherType.CLEAR_DAY
        }
        _currentWeather.value = nextWeather
    }

    fun setWeather(weather: WeatherType) {
        _currentWeather.value = weather
    }

    suspend fun triggerCloudSync() {
        _cloudSyncState.value = "Синхронизация..."
        kotlinx.coroutines.delay(800)
        val profile = dao.getPlayerProfile()
        if (profile != null) {
            dao.insertOrUpdateProfile(profile.copy(lastSyncTimestamp = System.currentTimeMillis()))
        }
        _cloudSyncState.value = "Синхронизировано"
    }

    fun restoreCloudSave(token: String): Boolean {
        if (token.isNotBlank()) {
            _cloudSyncState.value = "Облачный профиль восстановлен"
            return true
        }
        return false
    }

    fun sendChatMessage(text: String, channel: String) {
        if (text.isBlank()) return
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = "Вы (Валидор)",
            senderPlatform = "Android",
            channel = channel,
            text = text,
            timestamp = time,
            isGuildOfficer = true
        )
        _chatMessages.value = listOf(newMessage) + _chatMessages.value
    }

    fun addMarketListing(itemName: String, itemType: ItemType, rarity: ItemRarity, count: Int, price: Int) {
        val listing = MarketListing(
            id = UUID.randomUUID().toString(),
            sellerName = "Валидор (Вы)",
            sellerPlatform = "Android",
            itemName = itemName,
            itemType = itemType,
            rarity = rarity,
            count = count,
            priceGold = price,
            isPlayerListing = true,
            timeLeftMinutes = 120
        )
        _marketListings.value = listOf(listing) + _marketListings.value
    }

    fun buyMarketItem(id: String): MarketListing? {
        val item = _marketListings.value.find { it.id == id }
        if (item != null) {
            _marketListings.value = _marketListings.value.filter { it.id != id }
        }
        return item
    }

    fun captureTerritory(territoryId: String, newGuildName: String) {
        repositoryScope.launch {
            val list = dao.getAllTerritories().firstOrNull() ?: emptyList()
            val updated = list.map {
                if (it.territoryId == territoryId) {
                    it.copy(controllerGuild = newGuildName, defenseScore = 1000)
                } else it
            }
            dao.insertAllTerritories(updated)
        }
    }

    fun updateFactionReputation(faction: String, delta: Int) {
        val current = _factionReputations.value.toMutableMap()
        val prev = current[faction] ?: 0
        current[faction] = (prev + delta).coerceIn(-100, 100)
        _factionReputations.value = current
    }

    private fun defaultMarketListings(): List<MarketListing> {
        return listOf(
            MarketListing("m1", "Эльфийский_Снайпер", "PC", "Лунная стрела света", ItemType.WEAPON, ItemRarity.EPIC, 1, 320, false, 85),
            MarketListing("m2", "GamerPro99", "iOS", "Зелье неуязвимости", ItemType.POTION, ItemRarity.RARE, 3, 140, false, 40),
            MarketListing("m3", "МагОгня_Кирилл", "Android", "Ядро древнего пламени", ItemType.MATERIAL, ItemRarity.EPIC, 5, 290, false, 110),
            MarketListing("m4", "ShadowDagger", "PC", "Кинжал Призрака Тени", ItemType.WEAPON, ItemRarity.LEGENDARY, 1, 850, false, 24),
            MarketListing("m5", "Paladin_Arthur", "Android", "Священный щит Рассвета", ItemType.ARMOR, ItemRarity.EPIC, 1, 490, false, 75),
            MarketListing("m6", "ValkyrieQueen", "iOS", "Руна Титанической Силы", ItemType.RUNE, ItemRarity.LEGENDARY, 2, 600, false, 15)
        )
    }

    private fun defaultChatMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage("c1", "Командор_Эрик", "PC", "ГИЛЬДИЯ", "Сбор на осаду крепости Пик Дракона через 15 минут! Всем приготовить огненные зелья!", "18:42", true),
            ChatMessage("c2", "ЛеснаяФея", "Android", "ГИЛЬДИЯ", "Я накрафтила 20 эликсиров маны, выложила на рынок со скидкой для своих", "18:43"),
            ChatMessage("c3", "DarkSlayer_X", "iOS", "МИР", "Кто пойдет в кооп подземелье 'Логово Ледяного Дракона'? Нужен танк 4+ уровня!", "18:44"),
            ChatMessage("c4", "Система", "ALL", "СИСТЕМА", "Внимание: В мире начался 'Грозовой шторм'. Урон молнии усилен на 30%!", "18:45"),
            ChatMessage("c5", "Арканум", "PC", "МИР", "Продаю редкие руны зачарования, пишите в личку!", "18:46")
        )
    }

    private fun defaultCoopParty(): List<CoopPlayer> {
        return listOf(
            CoopPlayer("Валидор (Вы)", CharacterClass.WARRIOR, "Android", 3, 24, true, 1.0f),
            CoopPlayer("Селина_Маг", CharacterClass.MAGE, "PC", 4, 38, false, 0.92f),
            CoopPlayer("ТеневойЛинк", CharacterClass.ROGUE, "iOS", 3, 45, false, 0.85f),
            CoopPlayer("Брат_Иоанн", CharacterClass.PALADIN, "Android", 4, 18, false, 1.0f)
        )
    }

    private fun defaultSkillNodes(): List<SkillNode> {
        return listOf(
            SkillNode("sk_whirlwind", "Вихрь клинков", "БОЙ", "Круговой удар оружием, поражающий всех врагов вокруг.", 1, 15, 3.5f, true, "whirlwind"),
            SkillNode("sk_fireball", "Огненный шар", "МАГИЯ", "Выпускает взрывающийся сгусток пламени с поджиганием цели.", 1, 20, 2.0f, true, "fireball"),
            SkillNode("sk_frostnova", "Кольцо льда", "МАГИЯ", "Замораживает противников вокруг персонажа на 3 секунды.", 2, 25, 6.0f, true, "frostnova"),
            SkillNode("sk_thunder", "Громовой удар", "МАГИЯ", "Призывает мощный разряд молнии с небес на голову врага.", 3, 30, 8.0f, true, "thunder"),
            SkillNode("sk_heal", "Свет исцеления", "ВЫЖИВАНИЕ", "Восстанавливает 35% максимального здоровья и накладывает защитный барьер.", 2, 35, 12.0f, true, "heal"),
            SkillNode("sk_meteor", "Метеоритный дождь", "МАГИЯ", "Обрушивает град пылающих метеоритов на указанную область.", 5, 55, 20.0f, false, "meteor"),
            SkillNode("sk_shadowstep", "Шаг сквозь тень", "БОЙ", "Мгновенный рывок за спину противника с гарантированным критом.", 4, 20, 5.0f, false, "shadowstep")
        )
    }

    private fun defaultNpcCharacters(): List<NpcCharacter> {
        val seraphinaDialogues = mapOf(
            "start" to DialogueNode(
                id = "start",
                speakerName = "Архимаг Серафина",
                speakerRole = "Глава Верховного Совета Арканы",
                text = "Приветствую тебя, путник. Силовые линии Этерии дрожат. Кристалл Рассвета расколот, и сквозь трещины в наш мир просачиваются порождения Бездны. Готов ли ты обуздать древнюю магию стихий?",
                choices = listOf(
                    DialogueChoice("Я владею сталью и готов защищать земли Этерии!", "path_warrior", Pair("Королевская Стража", 10), xpReward = 50),
                    DialogueChoice("Обучи меня магии стихий. Я чувствую в себе силу арканы.", "path_mage", Pair("Ковен Арканы", 15), xpReward = 75),
                    DialogueChoice("Что вы можете заплатить за мои услуги?", "path_mercenary", Pair("Теневой Синдикат", 10), goldReward = 100)
                )
            ),
            "path_warrior" to DialogueNode(
                id = "path_warrior",
                speakerName = "Архимаг Серафина",
                speakerRole = "Глава Верховного Совета Арканы",
                text = "Благородный порыв! Командор Вейн в Цитадели как раз искал отважных воинов для зачистки Шепчущих лесов от гоблинов и нежити. Возьми этот оберег стойкости.",
                choices = listOf(
                    DialogueChoice("Принять оберег и отправиться на подвиги", "end", xpReward = 100)
                )
            ),
            "path_mage" to DialogueNode(
                id = "path_mage",
                speakerName = "Архимаг Серафина",
                speakerRole = "Глава Верховного Совета Арканы",
                text = "Искры стихий танцуют в твоих глазах. Помни: в грозу молнии и лед бьют сильнее, а ясным днем огонь выжигает врагов дотла. Возьми древний свиток Кольца Льда!",
                choices = listOf(
                    DialogueChoice("Изучить тайны стихийной погоды", "end", xpReward = 120)
                )
            ),
            "path_mercenary" to DialogueNode(
                id = "path_mercenary",
                speakerName = "Архимаг Серафина",
                speakerRole = "Глава Верховного Совета Арканы",
                text = "Прагматично. Золото правит миром не меньше магии. Вот твой аванс — уничтожь логово теневых тварей в Разломе, и получишь в десять раз больше.",
                choices = listOf(
                    DialogueChoice("Сделка заключена", "end", goldReward = 150)
                )
            ),
            "end" to DialogueNode(
                id = "end",
                speakerName = "Архимаг Серафина",
                speakerRole = "Глава Верховного Совета Арканы",
                text = "Да направят тебя Древние Звезды, герой. Возвращайся, когда совершишь подвиг.",
                choices = emptyList()
            )
        )

        val torvaldDialogues = mapOf(
            "start" to DialogueNode(
                id = "start",
                speakerName = "Кузнец Торвальд",
                speakerRole = "Великий мастер молота и горна",
                text = "Ха! Опять пришел чинить расколотый меч или хочешь сковать нечто воистину легендарное? Мой горн горит ярче полуденного солнца, были бы руда и руны!",
                choices = listOf(
                    DialogueChoice("Покажи свои лучшие рецепты клинков и доспехов.", "craft_info", xpReward = 40),
                    DialogueChoice("Как усилить оружие рунами зачарования?", "enhance_info", xpReward = 40),
                    DialogueChoice("Мне нужно улучшить снаряжение прямо сейчас.", "end")
                )
            ),
            "craft_info" to DialogueNode(
                id = "craft_info",
                speakerName = "Кузнец Торвальд",
                speakerRole = "Великий мастер молота",
                text = "Слушай сюда: собери железную руду в шахтах и ядра пламени с огненных тварей. С ними я скует тебе 'Двуручник Дракона' — он разрубит даже адамантовую броню!",
                choices = listOf(DialogueChoice("Спасибо за совет, мастер Торвальд!", "end"))
            ),
            "enhance_info" to DialogueNode(
                id = "enhance_info",
                speakerName = "Кузнец Торвальд",
                speakerRole = "Великий мастер молота",
                text = "Каждая вставленная руна дает +5 к базовой силе оружия. Но берегись: после +5 шанс заточки падает, используй защитные катализаторы!",
                choices = listOf(DialogueChoice("Я запомню это.", "end"))
            ),
            "end" to DialogueNode(
                id = "end",
                speakerName = "Кузнец Торвальд",
                speakerRole = "Мастер кузни",
                text = "Пусть сталь будет острой, а доспех — непробиваемым!",
                choices = emptyList()
            )
        )

        return listOf(
            NpcCharacter(
                id = "npc_seraphina",
                name = "Архимаг Серафина",
                role = "Верховный Хранитель Магии",
                faction = "Ковен Арканы",
                location = "Шпиль Высокой Цитадели",
                affinity = 65,
                dialogues = seraphinaDialogues,
                canRecruitAsCompanion = true,
                isCompanionRecruited = false
            ),
            NpcCharacter(
                id = "npc_torvald",
                name = "Кузнец Торвальд",
                role = "Мастер королевской оружейной",
                faction = "Королевская Стража",
                location = "Кузнечный квартал столицы",
                affinity = 50,
                dialogues = torvaldDialogues,
                canRecruitAsCompanion = false,
                isCompanionRecruited = false
            )
        )
    }
}

package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.GameRepository
import com.example.engine.ActionCombatEngine
import com.example.engine.EnemyType
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppNavTab(val title: String, val iconName: String) {
    WORLD("Мир и Бой", "explore"),
    CHARACTER("Герой", "person"),
    CRAFTING("Крафт", "hammer"),
    GUILD("Гильдия и Война", "shield"),
    STORY("Сюжет & NPC", "chat"),
    MARKET("Биржа & Чат", "store"),
    QUESTS("Квесты & Зал", "trophy")
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = GameRepository(db)
    val combatEngine = ActionCombatEngine()

    // Navigation
    private val _currentTab = MutableStateFlow(AppNavTab.WORLD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Active NPC dialogue
    private val _activeNpc = MutableStateFlow<NpcCharacter?>(null)
    val activeNpc: StateFlow<NpcCharacter?> = _activeNpc.asStateFlow()

    private val _currentDialogueNodeId = MutableStateFlow("start")
    val currentDialogueNodeId: StateFlow<String> = _currentDialogueNodeId.asStateFlow()

    // Toast/Alert banner
    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    // Customization dialog state
    private val _isCustomizationOpen = MutableStateFlow(false)
    val isCustomizationOpen: StateFlow<Boolean> = _isCustomizationOpen.asStateFlow()

    // Cloud dialog state
    private val _isCloudDialogOpen = MutableStateFlow(false)
    val isCloudDialogOpen: StateFlow<Boolean> = _isCloudDialogOpen.asStateFlow()

    // Room Database flows
    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inventory: StateFlow<List<InventoryEntity>> = repository.inventoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quests: StateFlow<List<QuestRecordEntity>> = repository.questsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementRecordEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val territories: StateFlow<List<TerritoryRecordEntity>> = repository.territoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Repository state flows
    val currentWeather: StateFlow<WeatherType> = repository.currentWeather
    val cloudSyncState: StateFlow<String> = repository.cloudSyncState
    val cloudSyncCode: StateFlow<String> = repository.cloudSyncCode
    val factionReputations: StateFlow<Map<String, Int>> = repository.factionReputations
    val marketListings: StateFlow<List<MarketListing>> = repository.marketListings
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val coopParty: StateFlow<List<CoopPlayer>> = repository.coopParty
    val skillTree: StateFlow<List<SkillNode>> = repository.skillTree
    val npcs: StateFlow<List<NpcCharacter>> = repository.npcs

    // In-game live combat gauges
    private val _liveHp = MutableStateFlow(270)
    val liveHp: StateFlow<Int> = _liveHp.asStateFlow()

    private val _liveMana = MutableStateFlow(70)
    val liveMana: StateFlow<Int> = _liveMana.asStateFlow()

    private val _liveStamina = MutableStateFlow(100)
    val liveStamina: StateFlow<Int> = _liveStamina.asStateFlow()

    init {
        // Wire combat callbacks
        combatEngine.onPlayerGainedXp = { amount ->
            gainXp(amount)
        }
        combatEngine.onPlayerGainedGold = { amount ->
            gainGold(amount)
        }
        combatEngine.onPlayerTookDamage = { dmg ->
            val cur = _liveHp.value
            _liveHp.value = (cur - dmg).coerceAtLeast(0)
            if (_liveHp.value == 0) {
                showAlert("Вы пали в бою! Возрождение у Алтаря Рассвета...")
                _liveHp.value = playerProfile.value?.maxHp ?: 250
                combatEngine.playerPos = androidx.compose.ui.geometry.Offset(700f, 700f)
            }
        }
        combatEngine.onEnemyKilled = { enemyType ->
            onEnemyDefeated(enemyType)
        }
        combatEngine.onResourceGathered = { type ->
            onHarvestNode(type)
        }

        // Start combat loop in coroutine
        viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (true) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastTime = now

                // Regenerate mana and stamina slowly
                val maxM = playerProfile.value?.maxMana ?: 70
                val maxS = playerProfile.value?.maxStamina ?: 100
                _liveMana.value = (_liveMana.value + 1).coerceAtMost(maxM)
                _liveStamina.value = (_liveStamina.value + 2).coerceAtMost(maxS)

                delay(30)
            }
        }

        // Auto cycle weather every 45 seconds for dynamic atmosphere
        viewModelScope.launch {
            while (true) {
                delay(45_000)
                repository.cycleWeather()
                combatEngine.currentWeather = repository.currentWeather.value
                showAlert("Погода изменилась: ${repository.currentWeather.value.title}! ${repository.currentWeather.value.magicBonusDesc}")
            }
        }
    }

    fun setTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun openCustomization() {
        _isCustomizationOpen.value = true
    }

    fun closeCustomization() {
        _isCustomizationOpen.value = false
    }

    fun openCloudDialog() {
        _isCloudDialogOpen.value = true
    }

    fun closeCloudDialog() {
        _isCloudDialogOpen.value = false
    }

    fun showAlert(msg: String) {
        _alertMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_alertMessage.value == msg) {
                _alertMessage.value = null
            }
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.triggerCloudSync()
            showAlert("Облачная синхронизация завершена успешно!")
        }
    }

    fun restoreCloudSave(token: String) {
        if (repository.restoreCloudSave(token)) {
            showAlert("Профиль $token успешно синхронизирован с облаком!")
            closeCloudDialog()
        } else {
            showAlert("Неверный токен синхронизации!")
        }
    }

    fun cycleWeatherManually() {
        repository.cycleWeather()
        combatEngine.currentWeather = repository.currentWeather.value
        showAlert("Погода: ${repository.currentWeather.value.title} (${repository.currentWeather.value.magicBonusDesc})")
    }

    fun performMeleeAttack() {
        val prof = playerProfile.value ?: return
        val str = prof.strength
        combatEngine.performMeleeAttack(str, weaponBonus = 25)
    }

    fun castSpell(spellId: String) {
        val prof = playerProfile.value ?: return
        val intel = prof.intelligence
        val curMana = _liveMana.value
        val cost = when (spellId) {
            "sk_fireball" -> 20
            "sk_frostnova" -> 25
            "sk_thunder" -> 30
            "sk_heal" -> 35
            else -> 20
        }
        if (curMana >= cost) {
            val casted = combatEngine.castSpell(spellId, intel, curMana)
            if (casted) {
                _liveMana.value -= cost
                if (spellId == "sk_heal") {
                    val healAmount = (prof.maxHp * 0.35f).toInt()
                    _liveHp.value = (_liveHp.value + healAmount).coerceAtMost(prof.maxHp)
                    showAlert("Свет исцеления: +$healAmount HP!")
                }
            }
        } else {
            showAlert("Недостаточно маны ($curMana/$cost)!")
        }
    }

    fun performDash() {
        val curStamina = _liveStamina.value
        if (curStamina >= 25) {
            if (combatEngine.performDash(curStamina)) {
                _liveStamina.value -= 25
            }
        } else {
            showAlert("Не хватает выносливости для рывка!")
        }
    }

    fun harvestNearby() {
        val gathered = combatEngine.harvestNearbyNode()
        if (gathered == null) {
            showAlert("Поблизости нет ресурсов для сбора. Подойдите к жиле руды или траве!")
        }
    }

    private fun onHarvestNode(type: String) {
        viewModelScope.launch {
            when (type) {
                "IRON" -> {
                    repository.addInventoryItem(
                        InventoryEntity(
                            itemId = "m_ironore",
                            name = "Магическая железная руда",
                            type = "MATERIAL",
                            count = 2,
                            rarity = "COMMON",
                            description = "Основной металл для ковки брони и клинков.",
                            sellPrice = 12
                        )
                    )
                    showAlert("Добыто: +2 Магической железной руды!")
                }
                "HERB" -> {
                    repository.addInventoryItem(
                        InventoryEntity(
                            itemId = "m_moonherb",
                            name = "Лунная трава",
                            type = "MATERIAL",
                            count = 2,
                            rarity = "RARE",
                            description = "Редкий алхимический цветок, распускающийся в сумерках.",
                            sellPrice = 25
                        )
                    )
                    showAlert("Собрано: +2 Лунной травы!")
                }
                "CHEST" -> {
                    gainGold(150)
                    repository.addInventoryItem(
                        InventoryEntity(
                            itemId = "r_attackrune",
                            name = "Руна ярости войны",
                            type = "RUNE",
                            count = 1,
                            rarity = "LEGENDARY",
                            description = "Вплавляется в оружие для улучшения до +1..+10.",
                            sellPrice = 150
                        )
                    )
                    showAlert("Открыт древний сундук: +150 Золота и Руна зачарования!")
                }
            }
        }
    }

    private fun onEnemyDefeated(type: EnemyType) {
        viewModelScope.launch {
            val qList = repository.questsFlow.firstOrNull() ?: emptyList()
            for (q in qList) {
                if (q.questId == "q_skeletons" && !q.isCompleted) {
                    val nextP = q.currentProgress + 1
                    val done = nextP >= 5
                    repository.updateQuest(q.copy(currentProgress = nextP, isCompleted = done))
                    if (done) showAlert("Квест выполнен: 'Уничтожить 5 скелетов'! Заберите награду!")
                }
            }
        }
    }

    private fun gainXp(amount: Int) {
        viewModelScope.launch {
            val prof = playerProfile.value ?: return@launch
            var newXp = prof.xp + amount
            var newLevel = prof.level
            var newPoints = prof.unspentPoints
            val xpNeeded = newLevel * 250
            if (newXp >= xpNeeded) {
                newXp -= xpNeeded
                newLevel++
                newPoints += 5
                val newMaxHp = prof.maxHp + 30
                val newMaxMana = prof.maxMana + 15
                _liveHp.value = newMaxHp
                _liveMana.value = newMaxMana
                repository.savePlayerProfile(
                    prof.copy(
                        level = newLevel,
                        xp = newXp,
                        unspentPoints = newPoints,
                        maxHp = newMaxHp,
                        maxMana = newMaxMana,
                        hp = newMaxHp,
                        mana = newMaxMana
                    )
                )
                showAlert("ПОЛУЧЕН НОВЫЙ УРОВЕНЬ: $newLevel! Получено +5 очков характеристик!")
            } else {
                repository.savePlayerProfile(prof.copy(xp = newXp))
            }
        }
    }

    private fun gainGold(amount: Int) {
        viewModelScope.launch {
            val prof = playerProfile.value ?: return@launch
            repository.savePlayerProfile(prof.copy(gold = prof.gold + amount))
        }
    }

    fun allocateAttribute(attr: String) {
        viewModelScope.launch {
            val prof = playerProfile.value ?: return@launch
            if (prof.unspentPoints <= 0) return@launch
            val updated = when (attr) {
                "STR" -> prof.copy(strength = prof.strength + 1, unspentPoints = prof.unspentPoints - 1)
                "AGI" -> prof.copy(agility = prof.agility + 1, unspentPoints = prof.unspentPoints - 1)
                "INT" -> prof.copy(
                    intelligence = prof.intelligence + 1,
                    maxMana = prof.maxMana + 5,
                    unspentPoints = prof.unspentPoints - 1
                )
                "VIT" -> prof.copy(
                    vitality = prof.vitality + 1,
                    maxHp = prof.maxHp + 15,
                    unspentPoints = prof.unspentPoints - 1
                )
                else -> prof
            }
            repository.savePlayerProfile(updated)
            showAlert("Характеристика $attr увеличена!")
        }
    }

    fun craftItem(recipe: CraftingRecipe) {
        viewModelScope.launch {
            val prof = playerProfile.value ?: return@launch
            if (prof.gold < recipe.requiredGold) {
                showAlert("Недостаточно золота для крафта (${recipe.requiredGold} G)!")
                return@launch
            }
            // Check inventory materials
            val inv = repository.inventoryFlow.firstOrNull() ?: emptyList()
            var canCraft = true
            for (req in recipe.requiredMaterials) {
                val found = inv.find { it.name.contains(req.first, ignoreCase = true) }
                if (found == null || found.count < req.second) {
                    canCraft = false
                    break
                }
            }
            if (!canCraft) {
                showAlert("Не хватает материалов для создания: ${recipe.resultItemName}!")
                return@launch
            }

            // Deduct gold
            repository.savePlayerProfile(prof.copy(gold = prof.gold - recipe.requiredGold))

            // Add result item
            repository.addInventoryItem(
                InventoryEntity(
                    itemId = UUID.randomUUID().toString(),
                    name = recipe.resultItemName,
                    type = recipe.resultType.name,
                    count = 1,
                    rarity = recipe.resultRarity.name,
                    description = recipe.description,
                    sellPrice = recipe.requiredGold * 2,
                    attackBonus = if (recipe.resultType == ItemType.WEAPON) 55 else 0,
                    defenseBonus = if (recipe.resultType == ItemType.ARMOR) 45 else 0
                )
            )
            showAlert("Успешно создано: ${recipe.resultItemName}!")
            gainXp(120)
        }
    }

    fun enhanceEquippedWeapon() {
        viewModelScope.launch {
            val prof = playerProfile.value ?: return@launch
            if (prof.gold < 150) {
                showAlert("Недостаточно золота для заточки (требуется 150 G)!")
                return@launch
            }
            val inv = repository.inventoryFlow.firstOrNull() ?: emptyList()
            val rune = inv.find { it.type == "RUNE" && it.count > 0 }
            if (rune == null) {
                showAlert("Для улучшения оружия требуется Руна зачарования!")
                return@launch
            }

            repository.savePlayerProfile(prof.copy(gold = prof.gold - 150))
            showAlert("Оружие '${prof.equippedWeapon}' успешно заточено руной ярости! Урон +15%!")
            gainXp(75)
        }
    }

    fun usePotion(potionType: String) {
        viewModelScope.launch {
            val prof = playerProfile.value ?: return@launch
            val inv = repository.inventoryFlow.firstOrNull() ?: emptyList()
            val potion = inv.find { it.type == "POTION" && it.count > 0 }
            if (potion != null) {
                if (potion.count > 1) {
                    repository.addInventoryItem(potion.copy(count = potion.count - 1))
                } else {
                    repository.removeInventoryItem(potion.id)
                }
                if (potionType == "HP") {
                    _liveHp.value = (_liveHp.value + 150).coerceAtMost(prof.maxHp)
                    showAlert("Использовано зелье: +150 HP восстановилось!")
                } else {
                    _liveMana.value = (_liveMana.value + 100).coerceAtMost(prof.maxMana)
                    showAlert("Использовано зелье: +100 Маны восстановилось!")
                }
            } else {
                showAlert("В инвентаре нет доступных зелий!")
            }
        }
    }

    fun captureTerritory(territoryId: String) {
        val prof = playerProfile.value ?: return
        repository.captureTerritory(territoryId, prof.guildName)
        showAlert("Крепость захвачена вашей гильдией '${prof.guildName}'! Налоги поступают в казну!")
        gainXp(200)
    }

    fun claimQuestReward(questId: String) {
        viewModelScope.launch {
            val qList = repository.questsFlow.firstOrNull() ?: emptyList()
            val q = qList.find { it.questId == questId }
            if (q != null && q.isCompleted && !q.isClaimed) {
                repository.updateQuest(q.copy(isClaimed = true))
                gainGold(300)
                gainXp(250)
                val prof = playerProfile.value ?: return@launch
                repository.savePlayerProfile(prof.copy(crystals = prof.crystals + 10))
                showAlert("Награда за квест получена: +300 Золота, +250 Опыта, +10 Кристаллов!")
            }
        }
    }

    fun claimAchievementReward(achievementId: String) {
        viewModelScope.launch {
            val aList = repository.achievementsFlow.firstOrNull() ?: emptyList()
            val a = aList.find { it.achievementId == achievementId }
            if (a != null && a.isUnlocked && !a.isClaimed) {
                repository.updateAchievement(a.copy(isClaimed = true))
                val prof = playerProfile.value ?: return@launch
                repository.savePlayerProfile(prof.copy(crystals = prof.crystals + 25))
                showAlert("Достижение забрано: +25 Кристаллов Этерии!")
            }
        }
    }

    fun startDialogueWithNpc(npc: NpcCharacter) {
        _activeNpc.value = npc
        _currentDialogueNodeId.value = "start"
        _currentTab.value = AppNavTab.STORY
    }

    fun selectDialogueChoice(choice: DialogueChoice) {
        if (choice.factionDelta != null) {
            repository.updateFactionReputation(choice.factionDelta.first, choice.factionDelta.second)
            showAlert("Репутация с '${choice.factionDelta.first}' изменена: +${choice.factionDelta.second}!")
        }
        if (choice.goldReward > 0) {
            gainGold(choice.goldReward)
        }
        if (choice.xpReward > 0) {
            gainXp(choice.xpReward)
        }
        _currentDialogueNodeId.value = choice.nextNodeId
    }

    fun closeDialogue() {
        _activeNpc.value = null
    }

    fun sendChatMessage(text: String, channel: String) {
        repository.sendChatMessage(text, channel)
    }

    fun postMarketListing(itemName: String, itemType: ItemType, rarity: ItemRarity, count: Int, price: Int) {
        repository.addMarketListing(itemName, itemType, rarity, count, price)
        showAlert("Товар '$itemName' успешно выставлен на биржу за $price G!")
    }

    fun buyMarketListing(listing: MarketListing) {
        viewModelScope.launch {
            val prof = playerProfile.value ?: return@launch
            if (prof.gold < listing.priceGold) {
                showAlert("Недостаточно золота для покупки (${listing.priceGold} G)!")
                return@launch
            }
            val bought = repository.buyMarketItem(listing.id)
            if (bought != null) {
                repository.savePlayerProfile(prof.copy(gold = prof.gold - listing.priceGold))
                repository.addInventoryItem(
                    InventoryEntity(
                        itemId = UUID.randomUUID().toString(),
                        name = bought.itemName,
                        type = bought.itemType.name,
                        count = bought.count,
                        rarity = bought.rarity.name,
                        description = "Куплено на бирже игроков у ${bought.sellerName}",
                        sellPrice = (bought.priceGold * 0.7f).toInt()
                    )
                )
                showAlert("Вы успешно приобрели '${bought.itemName}' у ${bought.sellerName}!")
            }
        }
    }

    fun startCoopRaid() {
        showAlert("Кооперативный рейд запущен! Синхронизация 4 платформ (Android / iOS / PC)...")
        combatEngine.resetWorld()
        // Spawn raid boss
        combatEngine.enemies.add(
            com.example.engine.CombatEnemy(
                id = "raid_boss_frost",
                type = EnemyType.ABYSSAL_OVERLORD,
                position = androidx.compose.ui.geometry.Offset(700f, 900f),
                currentHp = 1400
            )
        )
        _currentTab.value = AppNavTab.WORLD
    }

    fun updateCustomization(name: String, className: String, hairStyle: String, auraColorHex: Long) {
        viewModelScope.launch {
            val prof = playerProfile.value ?: return@launch
            val charClass = CharacterClass.values().find { it.name == className } ?: CharacterClass.WARRIOR
            repository.savePlayerProfile(
                prof.copy(
                    name = name,
                    characterClassName = className,
                    hairStyle = hairStyle,
                    auraColorHex = auraColorHex,
                    maxHp = charClass.baseHp,
                    maxMana = charClass.baseMana,
                    strength = charClass.baseStrength,
                    agility = charClass.baseAgility,
                    intelligence = charClass.baseIntelligence,
                    vitality = charClass.baseVitality
                )
            )
            _liveHp.value = charClass.baseHp
            _liveMana.value = charClass.baseMana
            closeCustomization()
            showAlert("Персонаж $name ($className) сохранен!")
        }
    }
}

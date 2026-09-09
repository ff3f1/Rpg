package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfileFlow(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfile(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM inventory_items ORDER BY id ASC")
    fun getAllInventoryItems(): Flow<List<InventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInventoryItems(items: List<InventoryEntity>)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItem(id: Long)

    @Query("DELETE FROM inventory_items")
    suspend fun clearInventory()

    @Query("SELECT * FROM quest_records")
    fun getAllQuests(): Flow<List<QuestRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuest(quest: QuestRecordEntity)

    @Query("SELECT * FROM achievement_records")
    fun getAllAchievements(): Flow<List<AchievementRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAchievement(achievement: AchievementRecordEntity)

    @Query("SELECT * FROM territory_records")
    fun getAllTerritories(): Flow<List<TerritoryRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTerritories(territories: List<TerritoryRecordEntity>)
}

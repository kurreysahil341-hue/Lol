package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<CommandHistoryEntity>>

    @Query("SELECT * FROM command_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteHistory(): Flow<List<CommandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CommandHistoryEntity): Long

    @Query("DELETE FROM command_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM command_history")
    suspend fun clearAllHistory()

    @Update
    suspend fun updateHistory(item: CommandHistoryEntity)

    // Contact Aliases
    @Query("SELECT * FROM contact_aliases ORDER BY aliasName ASC")
    fun getAllAliases(): Flow<List<ContactAliasEntity>>

    @Query("SELECT * FROM contact_aliases WHERE LOWER(aliasName) = LOWER(:alias) LIMIT 1")
    suspend fun findByAlias(alias: String): ContactAliasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: ContactAliasEntity): Long

    @Query("DELETE FROM contact_aliases WHERE id = :id")
    suspend fun deleteAliasById(id: Long)
}

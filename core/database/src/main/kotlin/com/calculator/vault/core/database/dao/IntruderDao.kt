package com.calculator.vault.core.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import com.calculator.vault.core.database.entity.IntruderAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntruderDao {
    @Query("SELECT * FROM intruder_attempts ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<IntruderAttemptEntity>>

    @Query("SELECT COALESCE(SUM(failed_attempts), 0) FROM intruder_attempts WHERE timestamp >= :since")
    fun observeFailedSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM intruder_attempts WHERE has_photo = 1")
    fun observePhotoCount(): Flow<Int>

    @Upsert
    suspend fun upsert(entity: IntruderAttemptEntity)

    @Query("SELECT * FROM intruder_attempts WHERE id = :id")
    suspend fun get(id: String): IntruderAttemptEntity?

    @Query("SELECT id FROM intruder_attempts WHERE has_photo = 1")
    suspend fun photoIds(): List<String>

    @Query("DELETE FROM intruder_attempts")
    suspend fun clear()
}

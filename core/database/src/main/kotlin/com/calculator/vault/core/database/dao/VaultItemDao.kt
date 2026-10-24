package com.calculator.vault.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.calculator.vault.core.database.entity.TypeCount
import com.calculator.vault.core.database.entity.VaultItemEntity
import com.calculator.vault.core.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultItemDao {
    @Query("SELECT * FROM vault_items WHERE type = :type AND trashed_at IS NULL ORDER BY date_taken DESC")
    fun observeByType(type: MediaType): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE trashed_at IS NOT NULL ORDER BY trashed_at DESC")
    fun observeTrash(): Flow<List<VaultItemEntity>>

    @Query("SELECT type, COUNT(*) AS count FROM vault_items WHERE trashed_at IS NULL GROUP BY type")
    fun observeCounts(): Flow<List<TypeCount>>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    fun observe(id: String): Flow<VaultItemEntity?>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun get(id: String): VaultItemEntity?

    @Query("SELECT * FROM vault_items WHERE id IN (:ids)")
    suspend fun getAll(ids: List<String>): List<VaultItemEntity>

    @Query("SELECT * FROM vault_items WHERE trashed_at IS NOT NULL AND trashed_at < :before")
    suspend fun trashedBefore(before: Long): List<VaultItemEntity>

    @Query("SELECT * FROM vault_items WHERE trashed_at IS NOT NULL")
    suspend fun allTrashed(): List<VaultItemEntity>

    @Query("SELECT COUNT(*) FROM vault_items")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(item: VaultItemEntity)

    @Upsert
    suspend fun upsertAll(items: List<VaultItemEntity>)

    @Query("UPDATE vault_items SET name = :name WHERE id = :id")
    suspend fun rename(id: String, name: String)

    @Query("UPDATE vault_items SET trashed_at = :at WHERE id IN (:ids)")
    suspend fun setTrashedAt(ids: List<String>, at: Long?)

    @Query("DELETE FROM vault_items WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("DELETE FROM vault_items")
    suspend fun clear()
}

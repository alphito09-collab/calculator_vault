package com.calculator.vault.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.calculator.vault.core.database.entity.BackupFileEntity

@Dao
interface BackupFileDao {
    @Query("SELECT * FROM backup_files")
    suspend fun all(): List<BackupFileEntity>

    @Upsert
    suspend fun upsert(entity: BackupFileEntity)

    @Query("DELETE FROM backup_files WHERE relative_path = :path")
    suspend fun delete(path: String)

    @Query("DELETE FROM backup_files")
    suspend fun clear()
}

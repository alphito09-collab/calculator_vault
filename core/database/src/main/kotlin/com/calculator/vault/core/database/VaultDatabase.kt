package com.calculator.vault.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.calculator.vault.core.database.dao.BackupFileDao
import com.calculator.vault.core.database.dao.IntruderDao
import com.calculator.vault.core.database.dao.VaultItemDao
import com.calculator.vault.core.database.entity.BackupFileEntity
import com.calculator.vault.core.database.entity.IntruderAttemptEntity
import com.calculator.vault.core.database.entity.VaultItemEntity
import com.calculator.vault.core.model.MediaType

@Database(
    entities = [VaultItemEntity::class, IntruderAttemptEntity::class, BackupFileEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun intruderDao(): IntruderDao
    abstract fun backupFileDao(): BackupFileDao

    companion object {
        const val NAME = "vault_index.db"
    }
}

internal class Converters {
    @TypeConverter fun fromType(type: MediaType): String = type.name
    @TypeConverter fun toType(value: String): MediaType = MediaType.valueOf(value)
}

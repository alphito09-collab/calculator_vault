package com.calculator.vault.core.database.di

import android.content.Context
import androidx.room.Room
import com.calculator.vault.core.database.VaultDatabase
import com.calculator.vault.core.database.dao.BackupFileDao
import com.calculator.vault.core.database.dao.IntruderDao
import com.calculator.vault.core.database.dao.VaultItemDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.calculator.vault.core.database")
class DatabaseModule {
    @Single
    fun database(context: Context): VaultDatabase =
        Room.databaseBuilder(context, VaultDatabase::class.java, VaultDatabase.NAME)
            // The index can always be rebuilt from the encrypted folder.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Single fun vaultItemDao(db: VaultDatabase): VaultItemDao = db.vaultItemDao()

    @Single fun intruderDao(db: VaultDatabase): IntruderDao = db.intruderDao()

    @Single fun backupFileDao(db: VaultDatabase): BackupFileDao = db.backupFileDao()
}

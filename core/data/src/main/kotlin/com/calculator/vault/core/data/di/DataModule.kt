package com.calculator.vault.core.data.di

import com.calculator.vault.core.database.di.DatabaseModule
import com.calculator.vault.core.datastore.di.DataStoreModule
import com.calculator.vault.core.network.di.NetworkModule
import com.calculator.vault.core.security.di.SecurityModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DataStoreModule::class, DatabaseModule::class, SecurityModule::class, NetworkModule::class])
@ComponentScan("com.calculator.vault.core.data")
class DataModule

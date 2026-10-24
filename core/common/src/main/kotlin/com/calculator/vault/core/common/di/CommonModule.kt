package com.calculator.vault.core.common.di

import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.calculator.vault.core.common")
class CommonModule {
    @Single
    fun dispatchers(): VaultDispatchers = VaultDispatchers.Default
}

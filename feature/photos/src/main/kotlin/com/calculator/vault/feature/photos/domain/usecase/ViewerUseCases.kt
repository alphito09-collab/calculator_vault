package com.calculator.vault.feature.photos.domain.usecase

import com.calculator.vault.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
class SlideshowIntervalUseCase(private val settings: SettingsRepository) {
    fun observe(): Flow<Int> = settings.settings.map { it.slideshowIntervalSeconds }
    suspend fun set(seconds: Int) = settings.setSlideshowInterval(seconds)
}

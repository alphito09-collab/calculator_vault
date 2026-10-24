package com.calculator.vault.core.domain.usecase

import com.calculator.vault.core.model.VaultItem
import org.koin.core.annotation.Factory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Day buckets for the "Today / 17 sep / 15 sep" grid headers, newest first. */
data class DateSection(val date: LocalDate, val items: List<VaultItem>)

@Factory
class GroupByDateUseCase {
    operator fun invoke(items: List<VaultItem>, zone: ZoneId = ZoneId.systemDefault()): List<DateSection> =
        items
            .sortedByDescending { it.dateTaken }
            .groupBy { Instant.ofEpochMilli(it.dateTaken).atZone(zone).toLocalDate() }
            .map { (date, list) -> DateSection(date, list) }
}

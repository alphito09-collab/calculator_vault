package com.calculator.vault.core.data.repository

import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.data.config.VaultConfigStore
import com.calculator.vault.core.data.storage.VaultFileStore
import com.calculator.vault.core.data.storage.VaultPaths
import com.calculator.vault.core.data.storage.writeAtomically
import com.calculator.vault.core.database.dao.IntruderDao
import com.calculator.vault.core.database.entity.IntruderAttemptEntity
import com.calculator.vault.core.database.entity.asModel
import com.calculator.vault.core.domain.repository.IntruderRepository
import com.calculator.vault.core.model.IntruderAttempt
import com.calculator.vault.core.model.IntruderStats
import com.calculator.vault.core.security.crypto.PublicKeyEncryptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Single(binds = [IntruderRepository::class])
class IntruderRepositoryImpl(
    private val dao: IntruderDao,
    private val configStore: VaultConfigStore,
    private val paths: VaultPaths,
    private val dispatchers: VaultDispatchers,
) : IntruderRepository {

    override val attempts: Flow<List<IntruderAttempt>> = dao.observeAll().map { list -> list.map { it.asModel() } }

    override val stats: Flow<IntruderStats> =
        combine(dao.observeFailedSince(startOfWeek()), dao.observePhotoCount()) { failed, photos ->
            IntruderStats(failedThisWeek = failed, photosCaptured = photos)
        }

    override suspend fun recordFailure(burstId: String, failedAttempts: Int, photoJpeg: ByteArray?) = withContext(dispatchers.io) {
        val id = burstId
        val existing = dao.get(id)
        var stored = existing?.hasPhoto == true
        if (photoJpeg != null && !stored) {
            val publicKeyset = configStore.current()?.publicKeyset
            if (publicKeyset != null) {
                runCatching {
                    val sealed = PublicKeyEncryptor(VaultKeyring.unb64(publicKeyset)).seal(photoJpeg, VaultFileStore.intruderContext(id))
                    paths.ensureDirs()
                    paths.intruderPhoto(id).writeAtomically { it.write(sealed) }
                    stored = true
                }.onFailure { Timber.e(it, "Storing intruder photo failed") }
            }
        }
        dao.upsert(IntruderAttemptEntity(id, existing?.timestamp ?: System.currentTimeMillis(), failedAttempts, stored))
    }

    override suspend fun clear() = withContext(dispatchers.io) {
        dao.photoIds().forEach { paths.locateIntruderPhoto(it).delete() }
        dao.clear()
    }

    private fun startOfWeek(): Long = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

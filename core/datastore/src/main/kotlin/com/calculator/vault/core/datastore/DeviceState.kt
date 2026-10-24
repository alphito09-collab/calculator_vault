package com.calculator.vault.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import java.util.Base64

/**
 * State bound to this device and install: Keystore-wrapped key copies and attempt counters.
 * Excluded from Auto Backup because Keystore keys cannot be restored on another device.
 */
@Single
class DeviceState(context: Context) {

    private val store: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { context.preferencesDataStoreFile(FILE_NAME) },
    )

    private val data = store.data.catch { emit(emptyPreferences()) }

    val hasBiometricKey: Flow<Boolean> = data.map { it[BIOMETRIC_WRAPPED] != null }

    suspend fun biometricWrapped(): Pair<ByteArray, ByteArray>? {
        val p = data.first()
        val wrapped = p[BIOMETRIC_WRAPPED] ?: return null
        val iv = p[BIOMETRIC_IV] ?: return null
        return decode(wrapped) to decode(iv)
    }

    suspend fun setBiometricWrapped(wrapped: ByteArray?, iv: ByteArray?) = store.edit {
        if (wrapped == null || iv == null) {
            it.remove(BIOMETRIC_WRAPPED); it.remove(BIOMETRIC_IV)
        } else {
            it[BIOMETRIC_WRAPPED] = encode(wrapped); it[BIOMETRIC_IV] = encode(iv)
        }
    }

    suspend fun deviceWrapped(): ByteArray? = data.first()[DEVICE_WRAPPED]?.let(::decode)

    suspend fun setDeviceWrapped(wrapped: ByteArray?) = store.edit {
        if (wrapped == null) it.remove(DEVICE_WRAPPED) else it[DEVICE_WRAPPED] = encode(wrapped)
    }

    suspend fun failedStreak(): Int = data.first()[FAILED_STREAK] ?: 0

    suspend fun setFailedStreak(value: Int) = store.edit { it[FAILED_STREAK] = value }

    private fun encode(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
    private fun decode(text: String): ByteArray = Base64.getDecoder().decode(text)

    companion object {
        const val FILE_NAME = "device_state"
        private val BIOMETRIC_WRAPPED = stringPreferencesKey("biometric_wrapped")
        private val BIOMETRIC_IV = stringPreferencesKey("biometric_iv")
        private val DEVICE_WRAPPED = stringPreferencesKey("device_wrapped")
        private val FAILED_STREAK = intPreferencesKey("failed_streak")
    }
}

package com.calculator.vault.feature.cloudbackup.presentation.mvi

import android.app.PendingIntent
import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.model.BackupNetwork
import com.calculator.vault.core.model.BackupSettings
import com.calculator.vault.core.model.BackupStatus
import com.calculator.vault.core.model.GoogleAccount
import com.calculator.vault.core.model.StorageQuota
import com.calculator.vault.core.network.drive.DriveAuth
import com.calculator.vault.core.network.drive.DriveToken
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.cloudbackup.domain.usecase.CloudBackupActionsUseCase
import com.calculator.vault.feature.cloudbackup.domain.usecase.ObserveCloudBackupUseCase
import com.google.android.gms.auth.api.identity.AuthorizationResult
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class CloudBackupState(
    val loading: Boolean = true,
    val settings: BackupSettings = BackupSettings(),
    val status: BackupStatus = BackupStatus.SignedOut,
    val online: Boolean = true,
    val quota: StorageQuota? = null,
    val busy: Boolean = false,
    val accountMenuOpen: Boolean = false,
    val restoring: Boolean = false,
) : UiState

sealed interface CloudBackupIntent : UiIntent {
    data object SignIn : CloudBackupIntent
    data class SignedIn(val account: GoogleAccount) : CloudBackupIntent
    data class ConsentResult(val result: AuthorizationResult?) : CloudBackupIntent
    data object SignOut : CloudBackupIntent
    data object BackupNow : CloudBackupIntent
    data object Restore : CloudBackupIntent
    data class Auto(val enabled: Boolean) : CloudBackupIntent
    data class Network(val network: BackupNetwork) : CloudBackupIntent
    data object OpenAccountMenu : CloudBackupIntent
    data object CloseAccountMenu : CloudBackupIntent
    data object Back : CloudBackupIntent
}

sealed interface CloudBackupEffect : UiEffect {
    data object LaunchSignIn : CloudBackupEffect
    data class RequestConsent(val intent: PendingIntent) : CloudBackupEffect
    data object SignedOut : CloudBackupEffect
    data class Restored(val count: Int) : CloudBackupEffect
    data class Error(val offline: Boolean) : CloudBackupEffect
    data object Close : CloudBackupEffect
}

@KoinViewModel
class CloudBackupViewModel(
    observe: ObserveCloudBackupUseCase,
    private val actions: CloudBackupActionsUseCase,
    private val driveAuth: DriveAuth,
) : MviViewModel<CloudBackupState, CloudBackupIntent, CloudBackupEffect>(CloudBackupState()) {

    init {
        observe().onEach { o ->
            setState { copy(loading = false, settings = o.settings, status = o.status, online = o.online) }
        }.launchIn(viewModelScope)
        observe().distinctUntilChangedBy { it.settings.accountEmail to it.online }.onEach { o ->
            if (o.settings.accountEmail != null && o.online) refreshQuota()
        }.launchIn(viewModelScope)
    }

    override fun handleIntent(intent: CloudBackupIntent) {
        when (intent) {
            CloudBackupIntent.SignIn -> if (!currentState.online) sendEffect(CloudBackupEffect.Error(offline = true)) else sendEffect(CloudBackupEffect.LaunchSignIn)
            is CloudBackupIntent.SignedIn -> viewModelScope.launch {
                setState { copy(busy = true) }
                runCatching { driveAuth.token() }
                    .onSuccess { token ->
                        when (token) {
                            is DriveToken.Granted -> finishSignIn(intent.account)
                            is DriveToken.NeedsConsent -> {
                                pendingAccount = intent.account
                                sendEffect(CloudBackupEffect.RequestConsent(token.intent))
                            }
                        }
                    }
                    .onFailure { sendEffect(CloudBackupEffect.Error(offline = false)) }
                setState { copy(busy = false) }
            }
            is CloudBackupIntent.ConsentResult -> viewModelScope.launch {
                val account = pendingAccount ?: return@launch
                val granted = intent.result?.let { runCatching { driveAuth.tokenFromResult(it) }.getOrNull() } is DriveToken.Granted
                if (granted) finishSignIn(account) else sendEffect(CloudBackupEffect.Error(offline = false))
                pendingAccount = null
            }
            CloudBackupIntent.SignOut -> viewModelScope.launch {
                setState { copy(accountMenuOpen = false, quota = null) }
                actions.signOut()
                sendEffect(CloudBackupEffect.SignedOut)
            }
            CloudBackupIntent.BackupNow -> viewModelScope.launch {
                if (!currentState.online) sendEffect(CloudBackupEffect.Error(offline = true)) else actions.backupNow()
            }
            CloudBackupIntent.Restore -> viewModelScope.launch {
                if (!currentState.online) return@launch sendEffect(CloudBackupEffect.Error(offline = true))
                setState { copy(restoring = true) }
                actions.restore()
                    .onSuccess { sendEffect(CloudBackupEffect.Restored(it)) }
                    .onFailure { sendEffect(CloudBackupEffect.Error(offline = false)) }
                setState { copy(restoring = false) }
            }
            is CloudBackupIntent.Auto -> viewModelScope.launch { actions.setAuto(intent.enabled) }
            is CloudBackupIntent.Network -> viewModelScope.launch { actions.setNetwork(intent.network) }
            CloudBackupIntent.OpenAccountMenu -> setState { copy(accountMenuOpen = true) }
            CloudBackupIntent.CloseAccountMenu -> setState { copy(accountMenuOpen = false) }
            CloudBackupIntent.Back -> sendEffect(CloudBackupEffect.Close)
        }
    }

    private var pendingAccount: GoogleAccount? = null

    private suspend fun finishSignIn(account: GoogleAccount) {
        actions.signedIn(account)
        actions.setAuto(true)
        refreshQuota()
    }

    private fun refreshQuota() {
        viewModelScope.launch { actions.quota().onSuccess { q -> setState { copy(quota = q) } } }
    }
}

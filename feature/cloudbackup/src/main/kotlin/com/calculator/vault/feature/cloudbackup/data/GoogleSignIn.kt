package com.calculator.vault.feature.cloudbackup.data

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.calculator.vault.core.model.GoogleAccount
import com.calculator.vault.feature.cloudbackup.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

sealed interface SignInResult {
    data class Success(val account: GoogleAccount) : SignInResult
    data object Cancelled : SignInResult
    data object NotConfigured : SignInResult
    data class Failed(val error: Throwable) : SignInResult
}

/** Google account picker through Credential Manager. Needs an Activity context. */
object GoogleSignIn {
    val isConfigured: Boolean get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    suspend fun signIn(activity: Activity): SignInResult {
        if (!isConfigured) return SignInResult.NotConfigured
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
        return try {
            val credential = CredentialManager.create(activity)
                .getCredential(activity, GetCredentialRequest.Builder().addCredentialOption(option).build())
                .credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                SignInResult.Success(GoogleAccount(google.id, google.displayName))
            } else {
                SignInResult.Failed(IllegalStateException("Unexpected credential ${credential.type}"))
            }
        } catch (_: GetCredentialCancellationException) {
            SignInResult.Cancelled
        } catch (error: Exception) {
            SignInResult.Failed(error)
        }
    }

    suspend fun signOut(activity: Activity) {
        runCatching { CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest()) }
    }
}

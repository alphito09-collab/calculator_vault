package com.calculator.vault.core.network.drive

import android.app.PendingIntent
import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Single
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed interface DriveToken {
    data class Granted(val accessToken: String) : DriveToken
    /** The user must consent once; launch [intent] from an Activity. */
    data class NeedsConsent(val intent: PendingIntent) : DriveToken
}

/** Obtains an OAuth access token for the Drive appDataFolder scope. Works from workers once granted. */
@Single
class DriveAuth(private val context: Context) {

    suspend fun token(): DriveToken {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DRIVE_APPDATA_SCOPE)))
            .build()
        val result = Identity.getAuthorizationClient(context).authorize(request).await()
        return result.toToken()
    }

    fun tokenFromResult(result: AuthorizationResult): DriveToken = result.toToken()

    private fun AuthorizationResult.toToken(): DriveToken {
        val intent = pendingIntent
        return when {
            hasResolution() && intent != null -> DriveToken.NeedsConsent(intent)
            accessToken != null -> DriveToken.Granted(accessToken!!)
            else -> throw IllegalStateException("No Drive access token")
        }
    }

    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }
}

internal suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}

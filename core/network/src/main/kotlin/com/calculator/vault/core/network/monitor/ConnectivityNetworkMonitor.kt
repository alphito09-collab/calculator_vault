package com.calculator.vault.core.network.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import com.calculator.vault.core.domain.repository.NetworkMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/** Emits the validated-internet and unmetered state of the default network. */
@Single(binds = [NetworkMonitor::class])
class ConnectivityNetworkMonitor(private val context: Context) : NetworkMonitor {

    private val capabilities: Flow<NetworkCapabilities?> = callbackFlow {
        val manager = context.getSystemService<ConnectivityManager>()
        if (manager == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps)
            }

            override fun onLost(network: Network) {
                trySend(null)
            }
        }
        trySend(manager.getNetworkCapabilities(manager.activeNetwork))
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.conflate()

    override val isOnline: Flow<Boolean> = capabilities
        .map { caps ->
            caps != null &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
        .distinctUntilChanged()

    override val isOnUnmeteredNetwork: Flow<Boolean> = capabilities
        .map { caps -> caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true }
        .distinctUntilChanged()
}

package com.calculator.vault.core.security.crypto

import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.hybrid.HybridConfig
import com.google.crypto.tink.streamingaead.StreamingAeadConfig

/** Registers the Tink primitives we use exactly once per process. */
internal object TinkBootstrap {
    @Volatile private var registered = false

    fun ensure() {
        if (registered) return
        synchronized(this) {
            if (registered) return
            AeadConfig.register()
            StreamingAeadConfig.register()
            HybridConfig.register()
            registered = true
        }
    }
}

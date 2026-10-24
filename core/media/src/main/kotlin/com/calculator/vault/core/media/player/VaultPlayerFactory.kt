package com.calculator.vault.core.media.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.calculator.vault.core.data.storage.VaultFileStore
import org.koin.core.annotation.Single

/** Builds ExoPlayers that read encrypted vault items. */
@Single
@UnstableApi
class VaultPlayerFactory(private val context: Context, private val store: VaultFileStore) {
    fun create(handleAudioFocus: Boolean = true): ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(EncryptedDataSource.Factory(store)))
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
            handleAudioFocus,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()
}

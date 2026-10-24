package com.calculator.vault.feature.videos.presentation.mvi

import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.domain.usecase.DecryptForSharingUseCase
import com.calculator.vault.core.domain.usecase.GroupByDateUseCase
import com.calculator.vault.core.domain.usecase.MoveToTrashUseCase
import com.calculator.vault.core.domain.usecase.ObserveVaultItemsUseCase
import com.calculator.vault.core.domain.usecase.UnhideItemsUseCase
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.ui.gallery.GalleryViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class VideosViewModel(
    observeItems: ObserveVaultItemsUseCase,
    groupByDate: GroupByDateUseCase,
    settings: SettingsRepository,
    moveToTrash: MoveToTrashUseCase,
    unhideItems: UnhideItemsUseCase,
    decryptForSharing: DecryptForSharingUseCase,
) : GalleryViewModel(MediaType.VIDEO, observeItems, groupByDate, settings, moveToTrash, unhideItems, decryptForSharing)

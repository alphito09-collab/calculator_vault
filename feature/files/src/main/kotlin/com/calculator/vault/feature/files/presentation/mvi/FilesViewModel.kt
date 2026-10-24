package com.calculator.vault.feature.files.presentation.mvi

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
class FilesViewModel(
    observeItems: ObserveVaultItemsUseCase,
    groupByDate: GroupByDateUseCase,
    settings: SettingsRepository,
    moveToTrash: MoveToTrashUseCase,
    unhideItems: UnhideItemsUseCase,
    decryptForSharing: DecryptForSharingUseCase,
) : GalleryViewModel(MediaType.FILE, observeItems, groupByDate, settings, moveToTrash, unhideItems, decryptForSharing)

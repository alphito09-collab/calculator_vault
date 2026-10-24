package com.calculator.vault.feature.files.presentation.mvi

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.usecase.DecryptForSharingUseCase
import com.calculator.vault.core.domain.usecase.MoveToTrashUseCase
import com.calculator.vault.core.domain.usecase.ObserveVaultItemUseCase
import com.calculator.vault.core.domain.usecase.UnhideItemsUseCase
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.files.domain.repository.DocumentRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import java.io.File

sealed interface FileContent {
    data object Loading : FileContent
    data class Pdf(val pageCount: Int) : FileContent
    data class Text(val text: String, val truncated: Boolean) : FileContent
    data object Image : FileContent
    data object Unsupported : FileContent
    data object Error : FileContent
}

data class FileViewerState(
    val item: VaultItem? = null,
    val content: FileContent = FileContent.Loading,
    val confirm: BulkAction? = null,
    val progress: TransferProgress? = null,
) : UiState

sealed interface FileViewerIntent : UiIntent {
    data object Share : FileViewerIntent
    data object Delete : FileViewerIntent
    data object Unhide : FileViewerIntent
    data object OpenWith : FileViewerIntent
    data object Confirm : FileViewerIntent
    data object Dismiss : FileViewerIntent
    data object Back : FileViewerIntent
}

sealed interface FileViewerEffect : UiEffect {
    data class ShareFiles(val files: List<File>, val mimeType: String) : FileViewerEffect
    data class OpenWith(val file: File, val mimeType: String) : FileViewerEffect
    data class Done(val action: BulkAction) : FileViewerEffect
    data object Failed : FileViewerEffect
    data object Close : FileViewerEffect
}

@KoinViewModel
class FileViewerViewModel(
    @InjectedParam private val itemId: String,
    observeItem: ObserveVaultItemUseCase,
    private val decrypt: DecryptForSharingUseCase,
    private val renderer: DocumentRenderer,
    private val moveToTrash: MoveToTrashUseCase,
    private val unhide: UnhideItemsUseCase,
) : MviViewModel<FileViewerState, FileViewerIntent, FileViewerEffect>(FileViewerState()) {

    private var plainFile: File? = null

    init {
        viewModelScope.launch {
            val item = observeItem(itemId).filterNotNull().first()
            setState { copy(item = item) }
            val file = decrypt(listOf(item.id)).getOrNull()?.firstOrNull()
            if (file == null) {
                setState { copy(content = FileContent.Error) }
                return@launch
            }
            plainFile = file
            val content = runCatching {
                when {
                    item.mimeType == "application/pdf" || item.extension == "pdf" -> FileContent.Pdf(renderer.open(file))
                    item.mimeType.startsWith("text/") || item.extension in TEXT_EXTENSIONS -> readText(file)
                    item.mimeType.startsWith("image/") -> FileContent.Image
                    else -> FileContent.Unsupported
                }
            }.getOrDefault(FileContent.Error)
            setState { copy(content = content) }
        }
    }

    suspend fun renderPage(index: Int, width: Int): Bitmap? = renderer.renderPage(index, width)

    override fun handleIntent(intent: FileViewerIntent) {
        val item = currentState.item ?: return
        when (intent) {
            FileViewerIntent.Share -> plainFile?.let { sendEffect(FileViewerEffect.ShareFiles(listOf(it), item.mimeType)) }
            FileViewerIntent.OpenWith -> plainFile?.let { sendEffect(FileViewerEffect.OpenWith(it, item.mimeType)) }
            FileViewerIntent.Delete -> setState { copy(confirm = BulkAction.Delete) }
            FileViewerIntent.Unhide -> setState { copy(confirm = BulkAction.Unhide) }
            FileViewerIntent.Dismiss -> setState { copy(confirm = null) }
            FileViewerIntent.Confirm -> confirm(item)
            FileViewerIntent.Back -> sendEffect(FileViewerEffect.Close)
        }
    }

    private fun confirm(item: VaultItem) {
        val action = currentState.confirm ?: return
        setState { copy(confirm = null) }
        viewModelScope.launch {
            renderer.close()
            when (action) {
                BulkAction.Delete -> moveToTrash(listOf(item.id))
                    .onSuccess { sendEffect(FileViewerEffect.Done(action)) }
                    .onFailure { sendEffect(FileViewerEffect.Failed) }
                BulkAction.Unhide -> {
                    setState { copy(progress = TransferProgress(0, 1)) }
                    unhide(listOf(item.id)).onEach { setState { copy(progress = it) } }.collect()
                    setState { copy(progress = null) }
                    sendEffect(FileViewerEffect.Done(action))
                }
                else -> Unit
            }
        }
    }

    private suspend fun readText(file: File): FileContent = withContext(Dispatchers.IO) {
        val bytes = file.inputStream().use { input -> input.readNBytes(MAX_TEXT_BYTES + 1) }
        FileContent.Text(bytes.copyOf(minOf(bytes.size, MAX_TEXT_BYTES)).decodeToString(), truncated = bytes.size > MAX_TEXT_BYTES)
    }

    override fun onCleared() = renderer.close()

    private companion object {
        const val MAX_TEXT_BYTES = 512 * 1024
        val TEXT_EXTENSIONS = setOf("txt", "csv", "md", "json", "log", "xml")
    }
}


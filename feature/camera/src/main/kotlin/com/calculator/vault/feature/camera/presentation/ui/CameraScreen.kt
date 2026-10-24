package com.calculator.vault.feature.camera.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.calculator.vault.core.common.session.LockGuard
import com.calculator.vault.core.designsystem.component.EmptyState
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultIconButton
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultThumbnailRequest
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.AppToasts
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.core.ui.util.ImmersiveMode
import com.calculator.vault.core.ui.util.formatDuration
import com.calculator.vault.feature.camera.R
import com.calculator.vault.feature.camera.presentation.components.ModeSwitch
import com.calculator.vault.feature.camera.presentation.components.ShutterButton
import com.calculator.vault.feature.camera.presentation.mvi.CameraEffect
import com.calculator.vault.feature.camera.presentation.mvi.CameraIntent
import com.calculator.vault.feature.camera.presentation.mvi.CameraState
import com.calculator.vault.feature.camera.presentation.mvi.CameraViewModel
import com.calculator.vault.feature.camera.presentation.mvi.CaptureMode
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraRoute(startWithVideo: Boolean, onClose: () -> Unit, viewModel: CameraViewModel = koinViewModel(key = "camera") { parametersOf(startWithVideo) }) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var hasCamera by remember { mutableStateOf(context.granted(Manifest.permission.CAMERA)) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasCamera = result[Manifest.permission.CAMERA] == true || context.granted(Manifest.permission.CAMERA)
    }
    LaunchedEffect(Unit) {
        if (!hasCamera) {
            LockGuard.allowExternalLaunch()
            permissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }
    val failed = stringResource(R.string.camera_failed)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            CameraEffect.Failed -> snackbar.showToast(failed, ToastKind.Error)
            is CameraEffect.Close -> {
                if (effect.savedCount > 0) AppToasts.show(context.resources.getQuantityString(R.plurals.camera_saved, effect.savedCount, effect.savedCount))
                onClose()
            }
        }
    }
    ImmersiveMode(enabled = true)
    androidx.activity.compose.BackHandler { viewModel.onIntent(CameraIntent.Close) }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCamera) {
            CameraContent(state, viewModel::onIntent)
        } else {
            Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyState(VaultIcons.Camera, stringResource(R.string.camera_permission_title), stringResource(R.string.camera_permission_message))
                VaultButton(stringResource(R.string.camera_open_settings), { ExternalIntents.openAppDetails(context) })
            }
            VaultIconButton(VaultIcons.Close, null, { viewModel.onIntent(CameraIntent.Close) }, Modifier.statusBarsPadding().padding(8.dp), tint = Color.White)
        }
        VaultSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun CameraContent(state: CameraState, onIntent: (CameraIntent) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { LifecycleCameraController(context) }
    var recording by remember { mutableStateOf<Recording?>(null) }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            recording?.stop()
            controller.unbind()
        }
    }
    LaunchedEffect(state.mode) {
        controller.setEnabledUseCases(if (state.mode == CaptureMode.PHOTO) CameraController.IMAGE_CAPTURE else CameraController.VIDEO_CAPTURE)
    }
    LaunchedEffect(state.frontLens) {
        val selector = if (state.frontLens) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        if (controller.hasCamera(selector)) controller.cameraSelector = selector
    }
    LaunchedEffect(state.flashOn, state.mode) {
        controller.imageCaptureFlashMode = if (state.flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        controller.enableTorch(state.flashOn && state.mode == CaptureMode.VIDEO)
    }

    val shutter: () -> Unit = {
        when (state.mode) {
            CaptureMode.PHOTO -> {
                val file = context.captureFile("IMG", "jpg")
                controller.takePicture(
                    ImageCapture.OutputFileOptions.Builder(file).build(),
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) = onIntent(CameraIntent.Captured(file, MediaType.PHOTO))
                        override fun onError(exception: ImageCaptureException) {
                            file.delete()
                            onIntent(CameraIntent.CaptureFailed)
                        }
                    },
                )
            }
            CaptureMode.VIDEO -> {
                val active = recording
                if (active != null) {
                    active.stop()
                    recording = null
                } else {
                    val file = context.captureFile("VID", "mp4")
                    val audio = AudioConfig.create(context.granted(Manifest.permission.RECORD_AUDIO))
                    recording = controller.startRecording(FileOutputOptions.Builder(file).build(), audio, ContextCompat.getMainExecutor(context)) { event ->
                        when (event) {
                            is VideoRecordEvent.Start -> onIntent(CameraIntent.RecordingStarted)
                            is VideoRecordEvent.Finalize -> {
                                onIntent(CameraIntent.RecordingStopped)
                                if (event.hasError() && file.length() == 0L) {
                                    file.delete()
                                    onIntent(CameraIntent.CaptureFailed)
                                } else {
                                    onIntent(CameraIntent.Captured(file, MediaType.VIDEO))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { PreviewView(it).apply { this.controller = controller; scaleType = PreviewView.ScaleType.FILL_CENTER } },
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VaultIconButton(VaultIcons.Close, stringResource(R.string.camera_close), { onIntent(CameraIntent.Close) }, tint = Color.White)
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(state.recording) {
                Text(
                    formatDuration(state.recordingSeconds * 1000L),
                    style = VaultTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.clip(VaultRadius.Pill).background(Color(0xFFE53935)).padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            VaultIconButton(if (state.flashOn) VaultIcons.Flash else VaultIcons.FlashOff, stringResource(R.string.camera_flash), { onIntent(CameraIntent.ToggleFlash) }, tint = Color.White)
        }
        val controls: @Composable () -> Unit = {
            Box(Modifier.size(52.dp).clip(VaultRadius.Tile).background(Color.White.copy(alpha = 0.15f))) {
                state.lastSavedId?.let { AsyncImage(VaultThumbnailRequest(it), null, Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop) }
            }
            ShutterButton(state.mode, state.recording, shutter, stringResource(R.string.camera_shutter))
            VaultIconButton(VaultIcons.SwitchCamera, stringResource(R.string.camera_switch), { onIntent(CameraIntent.SwitchLens) }, tint = Color.White, enabled = !state.recording)
        }
        if (isLandscape()) {
            Column(
                Modifier.align(Alignment.CenterEnd).fillMaxHeight().navigationBarsPadding().padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                controls()
                ModeSwitch(state.mode, stringResource(R.string.camera_photo), stringResource(R.string.camera_video), { onIntent(CameraIntent.SetMode(it)) }, !state.recording)
            }
        } else {
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.25f)).navigationBarsPadding().padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ModeSwitch(state.mode, stringResource(R.string.camera_photo), stringResource(R.string.camera_video), { onIntent(CameraIntent.SetMode(it)) }, !state.recording)
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 32.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    controls()
                }
            }
        }
        if (state.saving > 0) {
            Box(Modifier.align(Alignment.Center).size(12.dp).clip(CircleShape).background(VaultTheme.scheme.primary))
        }
    }
}

private fun Context.granted(permission: String) = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/** Plaintext capture lands in the private cache and is deleted right after encryption. */
private fun Context.captureFile(prefix: String, ext: String): File {
    val dir = File(cacheDir, "scratch").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File(dir, "${prefix}_$stamp.$ext")
}

package com.calculator.vault.core.media.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.calculator.vault.core.domain.repository.IntruderCamera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.math.max

/** Front-camera still capture with no preview, bound to the process lifecycle for one shot. */
@Single(binds = [IntruderCamera::class])
class CameraXIntruderCamera(private val context: Context) : IntruderCamera {

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override suspend fun captureSelfie(): ByteArray? = withContext(Dispatchers.Main) {
        if (!hasPermission()) return@withContext null
        val provider = runCatching { ProcessCameraProvider.awaitInstance(context) }.getOrNull() ?: return@withContext null
        val selector = CameraSelector.DEFAULT_FRONT_CAMERA
        if (!runCatching { provider.hasCamera(selector) }.getOrDefault(false)) return@withContext null
        val capture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
        try {
            provider.bindToLifecycle(ProcessLifecycleOwner.get(), selector, capture)
            delay(AUTO_EXPOSURE_SETTLE_MS)
            withTimeoutOrNull(CAPTURE_TIMEOUT_MS) { capture.takeJpeg() }
        } catch (error: Exception) {
            Timber.w(error, "Intruder capture failed")
            null
        } finally {
            runCatching { provider.unbind(capture) }
        }
    }

    private suspend fun ImageCapture.takeJpeg(): ByteArray? = suspendCancellableCoroutine { cont ->
        takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bytes = runCatching { image.toUprightJpeg() }.getOrNull()
                    image.close()
                    if (cont.isActive) cont.resume(bytes)
                }

                override fun onError(exception: ImageCaptureException) {
                    Timber.w(exception, "takePicture failed")
                    if (cont.isActive) cont.resume(null)
                }
            },
        )
    }

    private fun ImageProxy.toUprightJpeg(): ByteArray {
        val buffer = planes[0].buffer
        val raw = ByteArray(buffer.remaining()).also(buffer::get)
        val bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.size)
        val scale = MAX_EDGE.toFloat() / max(bitmap.width, bitmap.height)
        val matrix = Matrix().apply {
            postRotate(imageInfo.rotationDegrees.toFloat())
            postScale(-1f, 1f) // mirror like the user sees themselves
            if (scale < 1f) postScale(scale, scale)
        }
        val upright = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ByteArrayOutputStream().use { out ->
            upright.compress(Bitmap.CompressFormat.JPEG, 85, out)
            bitmap.recycle()
            if (upright != bitmap) upright.recycle()
            out.toByteArray()
        }
    }

    private companion object {
        const val AUTO_EXPOSURE_SETTLE_MS = 450L
        const val CAPTURE_TIMEOUT_MS = 4_000L
        const val MAX_EDGE = 1024
    }
}

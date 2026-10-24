package com.calculator.vault.core.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.calculator.vault.core.common.session.LockGuard
import timber.log.Timber
import java.io.File

/**
 * Launches system screens the vault returns from. Every launch is announced to [LockGuard]
 * so leaving for the share sheet doesn't re-lock the vault.
 */
object ExternalIntents {

    fun fileProviderAuthority(context: Context) = "${context.packageName}.fileprovider"

    fun share(context: Context, files: List<File>, mimeType: String = "*/*") {
        if (files.isEmpty()) return
        val uris = files.map { FileProvider.getUriForFile(context, fileProviderAuthority(context), it) }
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris.first())
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }.setType(mimeType).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        launch(context, Intent.createChooser(intent, null))
    }

    fun openWith(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, fileProviderAuthority(context), file)
        launch(context, Intent(Intent.ACTION_VIEW).setDataAndType(uri, mimeType).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
    }

    fun openUrl(context: Context, url: String) = launch(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    fun sendEmail(context: Context, to: String, subject: String, body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, body)
        launch(context, intent)
    }

    fun shareText(context: Context, text: String) {
        launch(context, Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
    }

    /** Opens the "All files access" toggle for this app (Android 11+) or app details. */
    fun openAllFilesAccessSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}"))
        } else {
            appDetails(context)
        }
        if (!launch(context, intent) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            launch(context, Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
    }

    fun openAppDetails(context: Context) = launch(context, appDetails(context))

    fun openUsageAccessSettings(context: Context) = launch(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))

    fun openOverlaySettings(context: Context) =
        launch(context, Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))

    private fun appDetails(context: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))

    /** Returns false when no app can handle [intent]. */
    fun launch(context: Context, intent: Intent): Boolean = try {
        LockGuard.allowExternalLaunch()
        context.startActivity(intent.addFlags(if (context is android.app.Activity) 0 else Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (error: ActivityNotFoundException) {
        Timber.w(error, "No activity for $intent")
        LockGuard.clear()
        false
    }
}

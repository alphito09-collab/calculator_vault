package com.calculator.vault.core.navigation

import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.RecoveryMethod
import kotlinx.serialization.Serializable

/**
 * Every destination in the app. Routes implementing [VaultRoute] show decrypted vault
 * content and are only reachable while the vault is unlocked.
 */
sealed interface AppRoute : NavKey

/** Marker for destinations behind the PIN. They are dropped from the back stack on lock. */
sealed interface VaultRoute : AppRoute

// region Launch, setup and lock screen
@Serializable data object SplashRoute : AppRoute

@Serializable data class LanguageRoute(val fromSettings: Boolean = false) : AppRoute

@Serializable data object OnboardingRoute : AppRoute

@Serializable data object CreatePinRoute : AppRoute

@Serializable data object CalculatorRoute : AppRoute

/** [method] was already chosen in the calculator's "Forgot PIN?" dialog; null shows every method. */
@Serializable data class ForgotPinRoute(val method: RecoveryMethod? = null) : AppRoute

@Serializable data class ResetPinRoute(val viaRecovery: Boolean = true) : AppRoute
// endregion

// region Recovery setup (first run is unprotected, from settings it is a vault route)
@Serializable data class RecoveryMethodRoute(val fromSettings: Boolean = false) : AppRoute

@Serializable data class PatternSetupRoute(val purpose: PatternPurpose) : AppRoute

@Serializable data class RecoveryEmailRoute(val fromSettings: Boolean = false) : AppRoute

@Serializable data class EmailOtpRoute(val email: String, val purpose: OtpPurpose) : AppRoute

@Serializable data class SecurityQuestionRoute(val fromSettings: Boolean = false) : AppRoute

@Serializable
enum class PatternPurpose { RECOVERY, APP_LOCK }

@Serializable
enum class OtpPurpose { VERIFY_RECOVERY_EMAIL, RESET_PIN }
// endregion

// region Vault
@Serializable
enum class MainTab { HOME, SETTINGS }

@Serializable data class MainRoute(val tab: MainTab = MainTab.HOME) : VaultRoute

@Serializable data class ImporterRoute(val filter: MediaType? = null) : VaultRoute

@Serializable data object PhotosRoute : VaultRoute

@Serializable data class PhotoViewerRoute(val itemId: String, val slideshow: Boolean = false) : VaultRoute

@Serializable data object VideosRoute : VaultRoute

@Serializable data class VideoPlayerRoute(val itemId: String) : VaultRoute

@Serializable data object AudiosRoute : VaultRoute

@Serializable data class AudioPlayerRoute(val itemId: String) : VaultRoute

@Serializable data object EqualizerRoute : VaultRoute

@Serializable data object FilesRoute : VaultRoute

@Serializable data class FileViewerRoute(val itemId: String) : VaultRoute

@Serializable data class CameraRoute(val startWithVideo: Boolean = false) : VaultRoute

@Serializable data object TrashRoute : VaultRoute

@Serializable data object ThemesRoute : VaultRoute

@Serializable data object AppLockRoute : VaultRoute

@Serializable data object AppLockSettingsRoute : VaultRoute

@Serializable data object IntruderRoute : VaultRoute

@Serializable data class IntruderPhotoRoute(val attemptId: String) : VaultRoute

@Serializable data object CloudBackupRoute : VaultRoute

@Serializable data object ChangePinRoute : VaultRoute

@Serializable data object ChangeAppIconRoute : VaultRoute

@Serializable data object VaultRecoverySettingsRoute : VaultRoute
// endregion

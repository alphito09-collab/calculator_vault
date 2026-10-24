package com.calculator.vault.core.common

object VaultConstants {
    /** Hidden folder in shared storage that survives uninstall. */
    const val VAULT_DIR_NAME = ".calculator_vault_data"
    const val PIN_LENGTH = 4
    /** Wrong PINs before an intruder is logged (and photographed); the user picks 1–6. */
    const val DEFAULT_INTRUDER_ATTEMPTS = 3
    const val MIN_INTRUDER_ATTEMPTS = 1
    const val MAX_INTRUDER_ATTEMPTS = 6

    /** Wrong PINs before "Forgot PIN?" is offered while intruder detection is off. */
    const val FORGOT_PIN_ATTEMPTS = 3

    /** With intruder detection on, "Forgot PIN?" waits this many attempts past the intruder limit. */
    const val FORGOT_PIN_ATTEMPTS_AFTER_INTRUDER = 2

    /** Typed on the calculator followed by "=", opens "set a new PIN" (only on this install). */
    const val RESET_PIN_CODE = "7860786"
    const val TRASH_RETENTION_DAYS = 30
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id="
    const val SUPPORT_EMAIL = "support@alphito.app"
    const val PRIVACY_POLICY_URL = "https://alphito.app/calculator-vault/privacy"
    const val TERMS_URL = "https://alphito.app/calculator-vault/terms"

    /** Launcher `<activity-alias>` classes live under the app namespace (not the applicationId). */
    const val ALIAS_CLASS_PREFIX = "com.calculator.vault.hide.app.lock.photo.video.free.alias."
}

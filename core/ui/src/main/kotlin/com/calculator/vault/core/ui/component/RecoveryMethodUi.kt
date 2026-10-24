package com.calculator.vault.core.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.ui.R

/** "Draw your pattern", "Answer your security question"… shared by the forgot-PIN dialog and screen. */
@StringRes
fun RecoveryMethod.actionRes(): Int = when (this) {
    RecoveryMethod.FINGERPRINT -> R.string.ui_recovery_use_fingerprint
    RecoveryMethod.PATTERN -> R.string.ui_recovery_use_pattern
    RecoveryMethod.EMAIL -> R.string.ui_recovery_use_email
    RecoveryMethod.SECURITY_QUESTION -> R.string.ui_recovery_use_question
}

@DrawableRes
fun RecoveryMethod.iconRes(): Int = when (this) {
    RecoveryMethod.FINGERPRINT -> VaultIcons.FingerprintLine
    RecoveryMethod.PATTERN -> VaultIcons.PatternSmall
    RecoveryMethod.EMAIL -> VaultIcons.Mail
    RecoveryMethod.SECURITY_QUESTION -> VaultIcons.HelpCircle
}

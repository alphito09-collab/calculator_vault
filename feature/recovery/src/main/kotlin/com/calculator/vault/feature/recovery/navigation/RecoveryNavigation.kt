package com.calculator.vault.feature.recovery.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.EmailOtpRoute
import com.calculator.vault.core.navigation.ForgotPinRoute
import com.calculator.vault.core.navigation.MainRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.OtpPurpose
import com.calculator.vault.core.navigation.PatternPurpose
import com.calculator.vault.core.navigation.PatternSetupRoute
import com.calculator.vault.core.navigation.RecoveryEmailRoute
import com.calculator.vault.core.navigation.RecoveryMethodRoute
import com.calculator.vault.core.navigation.ResetPinRoute
import com.calculator.vault.core.navigation.SecurityQuestionRoute
import com.calculator.vault.feature.recovery.domain.usecase.PatternTarget
import com.calculator.vault.feature.recovery.presentation.ui.EmailOtpRoute as EmailOtpScreenRoute
import com.calculator.vault.feature.recovery.presentation.ui.ForgotPinRoute as ForgotPinScreenRoute
import com.calculator.vault.feature.recovery.presentation.ui.PatternSetupRoute as PatternSetupScreenRoute
import com.calculator.vault.feature.recovery.presentation.ui.RecoveryEmailRoute as RecoveryEmailScreenRoute
import com.calculator.vault.feature.recovery.presentation.ui.RecoveryMethodRoute as RecoveryMethodScreenRoute
import com.calculator.vault.feature.recovery.presentation.ui.SecurityQuestionRoute as SecurityQuestionScreenRoute

fun EntryProviderScope<NavKey>.recoveryEntries(navigator: Navigator) {
    entry<RecoveryMethodRoute> { route ->
        RecoveryMethodScreenRoute(
            fromSettings = route.fromSettings,
            onOpenPattern = { navigator.navigate(PatternSetupRoute(PatternPurpose.RECOVERY)) },
            onOpenEmail = { navigator.navigate(RecoveryEmailRoute(route.fromSettings)) },
            onOpenSecurityQuestion = { navigator.navigate(SecurityQuestionRoute(route.fromSettings)) },
            onFinish = { if (route.fromSettings) navigator.back() else navigator.resetTo(MainRoute()) },
        )
    }
    entry<PatternSetupRoute> { route ->
        PatternSetupScreenRoute(
            target = if (route.purpose == PatternPurpose.APP_LOCK) PatternTarget.APP_LOCK else PatternTarget.RECOVERY,
            onSaved = { navigator.back() },
            onClose = { navigator.back() },
        )
    }
    entry<RecoveryEmailRoute> {
        RecoveryEmailScreenRoute(
            onCodeSent = { email -> navigator.replaceTop(EmailOtpRoute(email, OtpPurpose.VERIFY_RECOVERY_EMAIL)) },
            onClose = { navigator.back() },
        )
    }
    entry<EmailOtpRoute> { route ->
        EmailOtpScreenRoute(
            email = route.email,
            purpose = route.purpose,
            onEmailSaved = { navigator.back() },
            onVaultUnlocked = { navigator.resetTo(ResetPinRoute()) },
            onClose = { navigator.back() },
        )
    }
    entry<SecurityQuestionRoute> {
        SecurityQuestionScreenRoute(onSaved = { navigator.back() }, onClose = { navigator.back() })
    }
    entry<ForgotPinRoute> { route ->
        ForgotPinScreenRoute(
            method = route.method,
            onRecovered = { navigator.resetTo(ResetPinRoute()) },
            onOtpSent = { email -> navigator.navigate(EmailOtpRoute(email, OtpPurpose.RESET_PIN)) },
            onClose = { navigator.back() },
        )
    }
}

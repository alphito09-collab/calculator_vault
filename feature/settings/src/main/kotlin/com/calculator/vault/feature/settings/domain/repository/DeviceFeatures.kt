package com.calculator.vault.feature.settings.domain.repository

import android.content.Intent
import com.calculator.vault.core.model.AppIconOption

/** Switches the launcher icon/label by enabling one activity-alias. */
interface AppIconSwitcher {
    fun current(): AppIconOption
    fun apply(option: AppIconOption)
}

/** Device-admin based uninstall protection. */
interface UninstallProtection {
    fun isActive(): Boolean
    fun activationIntent(explanation: String): Intent
    fun deactivate()
}

package com.calculator.vault.feature.settings.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.feature.settings.domain.repository.AppIconSwitcher
import org.koin.core.annotation.Single
import timber.log.Timber

/**
 * Exactly one launcher alias is enabled at a time. The new alias is enabled before the old
 * ones are disabled so the app always has a launcher entry.
 */
@Single(binds = [AppIconSwitcher::class])
class AppIconSwitcherImpl(private val context: Context) : AppIconSwitcher {

    private fun component(option: AppIconOption) =
        ComponentName(context.packageName, VaultConstants.ALIAS_CLASS_PREFIX + option.aliasName)

    override fun current(): AppIconOption = AppIconOption.entries.firstOrNull { option ->
        val state = context.packageManager.getComponentEnabledSetting(component(option))
        state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
            (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && option == AppIconOption.DEFAULT)
    } ?: AppIconOption.DEFAULT

    override fun apply(option: AppIconOption) {
        val pm = context.packageManager
        runCatching {
            pm.setComponentEnabledSetting(component(option), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            AppIconOption.entries.filter { it != option }.forEach { other ->
                pm.setComponentEnabledSetting(component(other), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            }
        }.onFailure { Timber.e(it, "Switching launcher icon failed") }
    }
}

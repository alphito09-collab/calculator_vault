package com.calculator.vault.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * Single source of truth for the back stack, scoped to the process (not to an Activity) so it
 * survives configuration changes but is deliberately *not* restored after process death:
 * a fresh process always starts at the splash screen, which keeps the vault locked.
 */
class Navigator {

    val backStack: SnapshotStateList<NavKey> = mutableStateListOf(SplashRoute)

    /** Vault back stack captured on lock, restored after the next successful unlock. */
    private var lockedVaultStack: List<NavKey>? = null

    val currentRoute: NavKey? get() = backStack.lastOrNull()

    fun navigate(route: NavKey) {
        if (backStack.lastOrNull() == route) return
        backStack.add(route)
    }

    /** Pops the top entry. Returns false when there is nothing left to pop. */
    fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    fun replaceTop(route: NavKey) {
        if (backStack.isEmpty()) backStack.add(route) else backStack[backStack.lastIndex] = route
    }

    fun resetTo(vararg routes: NavKey) {
        val newStack = routes.toList()
        // Mutate in place (add first, then trim) so NavDisplay animates instead of flashing empty.
        backStack.addAll(newStack)
        repeat(backStack.size - newStack.size) { backStack.removeAt(0) }
    }

    /** Pops entries until [predicate] matches the top entry (the match itself stays). */
    fun popUntil(predicate: (NavKey) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) backStack.removeAt(backStack.lastIndex)
    }

    // region Vault lock handling
    val isInsideVault: Boolean get() = backStack.any { it is VaultRoute }

    /** Saves the current vault screens and shows the splash → calculator flow again. */
    fun lockVault() {
        if (isInsideVault) lockedVaultStack = backStack.filterIsInstance<VaultRoute>()
        resetTo(SplashRoute)
    }

    /** Called after a successful unlock: returns to exactly where the user left the vault. */
    fun openVault() {
        val restore = lockedVaultStack?.takeIf { it.isNotEmpty() } ?: listOf(MainRoute())
        lockedVaultStack = null
        resetTo(*restore.toTypedArray())
    }

    fun discardLockedVaultStack() {
        lockedVaultStack = null
    }
    // endregion
}

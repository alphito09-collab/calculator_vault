package com.calculator.vault.core.model

/** Security questions offered as a recovery method. Text lives in string resources. */
enum class SecurityQuestion {
    FIRST_PET,
    BIRTH_CITY,
    FIRST_SCHOOL,
    CHILDHOOD_FRIEND,
    FAVORITE_TEACHER,
    MOTHER_MAIDEN_NAME,
}

/** Which recovery methods are configured. */
data class RecoveryOptions(
    val fingerprintEnabled: Boolean = false,
    val hasPattern: Boolean = false,
    val email: String? = null,
    val securityQuestion: SecurityQuestion? = null,
) {
    val hasAnyMethod: Boolean
        get() = fingerprintEnabled || hasPattern || email != null || securityQuestion != null
}

sealed interface PinCheckResult {
    data object Success : PinCheckResult
    data class Wrong(val consecutiveFailures: Int) : PinCheckResult
    data object NotAPin : PinCheckResult
}

/** Recovery methods usable from the "forgot PIN" flow on this install. */
enum class RecoveryMethod { FINGERPRINT, PATTERN, EMAIL, SECURITY_QUESTION }

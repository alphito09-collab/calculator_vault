package com.calculator.vault.feature.language.domain.model

/** A selectable UI language. [tag] null means "follow the system". */
data class AppLanguage(
    val tag: String?,
    val englishName: String,
    val nativeName: String,
    val flag: String,
)

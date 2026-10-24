package com.calculator.vault.core.model

enum class AppIconCategory { DEFAULT, CALCULATOR, MUSIC, NOTES, MAPS }

/**
 * Launcher disguises. Each option maps 1:1 to an `<activity-alias>` named
 * `${applicationId}.alias.<aliasName>` in the app manifest.
 */
enum class AppIconOption(val category: AppIconCategory, val aliasName: String) {
    DEFAULT(AppIconCategory.DEFAULT, "Default"),
    CALCULATOR_1(AppIconCategory.CALCULATOR, "Calculator1"),
    CALCULATOR_2(AppIconCategory.CALCULATOR, "Calculator2"),
    CALCULATOR_3(AppIconCategory.CALCULATOR, "Calculator3"),
    CALCULATOR_4(AppIconCategory.CALCULATOR, "Calculator4"),
    MUSIC_1(AppIconCategory.MUSIC, "Music1"),
    MUSIC_2(AppIconCategory.MUSIC, "Music2"),
    MUSIC_3(AppIconCategory.MUSIC, "Music3"),
    MUSIC_4(AppIconCategory.MUSIC, "Music4"),
    NOTES_1(AppIconCategory.NOTES, "Notes1"),
    NOTES_2(AppIconCategory.NOTES, "Notes2"),
    NOTES_3(AppIconCategory.NOTES, "Notes3"),
    NOTES_4(AppIconCategory.NOTES, "Notes4"),
    MAPS_1(AppIconCategory.MAPS, "Maps1"),
    MAPS_2(AppIconCategory.MAPS, "Maps2"),
    MAPS_3(AppIconCategory.MAPS, "Maps3"),
    MAPS_4(AppIconCategory.MAPS, "Maps4"),
}

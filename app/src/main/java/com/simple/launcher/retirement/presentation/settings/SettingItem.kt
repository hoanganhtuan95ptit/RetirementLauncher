package com.simple.launcher.retirement.presentation.settings

data class SettingItem(
    val id: Int,
    val title: String,
    val iconRes: Int,
    val isSwitch: Boolean = false,
    var isChecked: Boolean = false
) {
    companion object {
        const val ID_PIN = 1
        const val ID_APP_LIST = 2
        const val ID_DEFAULT_LAUNCHER = 3
        const val ID_CLEAN_FILES = 4
        const val ID_CLEAN_MEMORY = 5
        const val ID_CONTACT_LIST = 6
        const val ID_TOGGLE_BLOCK = 7
        const val ID_TOGGLE_CLEANUP = 8
        const val ID_TOGGLE_CALL_BLOCK = 9
    }
}

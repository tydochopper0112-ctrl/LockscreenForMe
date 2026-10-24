package com.fold.lockscreen.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lockscreen_prefs")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val CLOCK_SIZE_SCALE = floatPreferencesKey("clock_size_scale")
        val CLOCK_COLOR = longPreferencesKey("clock_color")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val BACKGROUND_COLOR = longPreferencesKey("background_color")

        // Cover Screen (Outer) Layout
        val COVER_CLOCK_X = floatPreferencesKey("cover_clock_x")
        val COVER_CLOCK_Y = floatPreferencesKey("cover_clock_y")
        val COVER_NOTIF_X = floatPreferencesKey("cover_notif_x")
        val COVER_NOTIF_Y = floatPreferencesKey("cover_notif_y")

        // Main Screen (Inner) Layout
        val MAIN_CLOCK_X = floatPreferencesKey("main_clock_x")
        val MAIN_CLOCK_Y = floatPreferencesKey("main_clock_y")
        val MAIN_NOTIF_X = floatPreferencesKey("main_notif_x")
        val MAIN_NOTIF_Y = floatPreferencesKey("main_notif_y")
        val MAIN_TASKBAR_PADDING = intPreferencesKey("main_taskbar_padding")

        val NOTIFICATION_MODE = stringPreferencesKey("notification_mode")
        val DOUBLE_TAP_PACKAGE = stringPreferencesKey("double_tap_package")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
    }

    val settingsFlow: Flow<LockscreenSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val clockStyleStr = preferences[PreferencesKeys.CLOCK_STYLE] ?: ClockStyle.DIGITAL_MINIMAL.name
            val fontStyleStr = preferences[PreferencesKeys.FONT_STYLE] ?: FontStyle.MONOSPACE.name
            val notificationModeStr = preferences[PreferencesKeys.NOTIFICATION_MODE] ?: NotificationDisplayMode.COMPACT_LIST.name

            val clockStyle = runCatching { ClockStyle.valueOf(clockStyleStr) }.getOrDefault(ClockStyle.DIGITAL_MINIMAL)
            val fontStyle = runCatching { FontStyle.valueOf(fontStyleStr) }.getOrDefault(FontStyle.MONOSPACE)
            val notifMode = runCatching { NotificationDisplayMode.valueOf(notificationModeStr) }.getOrDefault(NotificationDisplayMode.COMPACT_LIST)

            val coverLayout = ScreenLayoutConfig(
                clockXPercent = preferences[PreferencesKeys.COVER_CLOCK_X] ?: 0.5f,
                clockYPercent = preferences[PreferencesKeys.COVER_CLOCK_Y] ?: 0.32f,
                notificationXPercent = preferences[PreferencesKeys.COVER_NOTIF_X] ?: 0.5f,
                notificationYPercent = preferences[PreferencesKeys.COVER_NOTIF_Y] ?: 0.60f,
                taskbarPaddingBottomDp = 32
            )

            val mainLayout = ScreenLayoutConfig(
                clockXPercent = preferences[PreferencesKeys.MAIN_CLOCK_X] ?: 0.5f,
                clockYPercent = preferences[PreferencesKeys.MAIN_CLOCK_Y] ?: 0.36f,
                notificationXPercent = preferences[PreferencesKeys.MAIN_NOTIF_X] ?: 0.5f,
                notificationYPercent = preferences[PreferencesKeys.MAIN_NOTIF_Y] ?: 0.64f,
                taskbarPaddingBottomDp = preferences[PreferencesKeys.MAIN_TASKBAR_PADDING] ?: 64
            )

            LockscreenSettings(
                clockStyle = clockStyle,
                fontStyle = fontStyle,
                clockSizeScale = preferences[PreferencesKeys.CLOCK_SIZE_SCALE] ?: 1.0f,
                clockColor = preferences[PreferencesKeys.CLOCK_COLOR] ?: 0xFFFFFFFF,
                accentColor = preferences[PreferencesKeys.ACCENT_COLOR] ?: 0xFF00E5FF,
                backgroundColor = preferences[PreferencesKeys.BACKGROUND_COLOR] ?: 0xFF000000,
                coverLayout = coverLayout,
                mainLayout = mainLayout,
                notificationMode = notifMode,
                doubleTapPackage = preferences[PreferencesKeys.DOUBLE_TAP_PACKAGE] ?: "com.sec.android.app.clockpackage",
                showSeconds = preferences[PreferencesKeys.SHOW_SECONDS] ?: true,
                is24Hour = preferences[PreferencesKeys.IS_24_HOUR] ?: true
            )
        }

    suspend fun updateClockStyle(style: ClockStyle) {
        context.dataStore.edit { it[PreferencesKeys.CLOCK_STYLE] = style.name }
    }

    suspend fun updateFontStyle(font: FontStyle) {
        context.dataStore.edit { it[PreferencesKeys.FONT_STYLE] = font.name }
    }

    suspend fun updateClockSize(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.CLOCK_SIZE_SCALE] = scale }
    }

    suspend fun updateColors(clockColor: Long, accentColor: Long, backgroundColor: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.CLOCK_COLOR] = clockColor
            it[PreferencesKeys.ACCENT_COLOR] = accentColor
            it[PreferencesKeys.BACKGROUND_COLOR] = backgroundColor
        }
    }

    suspend fun updateCoverLayout(clockX: Float, clockY: Float, notifX: Float, notifY: Float) {
        context.dataStore.edit {
            it[PreferencesKeys.COVER_CLOCK_X] = clockX
            it[PreferencesKeys.COVER_CLOCK_Y] = clockY
            it[PreferencesKeys.COVER_NOTIF_X] = notifX
            it[PreferencesKeys.COVER_NOTIF_Y] = notifY
        }
    }

    suspend fun updateMainLayout(clockX: Float, clockY: Float, notifX: Float, notifY: Float, taskbarPaddingDp: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.MAIN_CLOCK_X] = clockX
            it[PreferencesKeys.MAIN_CLOCK_Y] = clockY
            it[PreferencesKeys.MAIN_NOTIF_X] = notifX
            it[PreferencesKeys.MAIN_NOTIF_Y] = notifY
            it[PreferencesKeys.MAIN_TASKBAR_PADDING] = taskbarPaddingDp
        }
    }

    suspend fun updateNotificationMode(mode: NotificationDisplayMode) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATION_MODE] = mode.name }
    }

    suspend fun updateDoubleTapPackage(pkg: String) {
        context.dataStore.edit { it[PreferencesKeys.DOUBLE_TAP_PACKAGE] = pkg }
    }

    suspend fun updateTimeFormat(is24Hour: Boolean, showSeconds: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.IS_24_HOUR] = is24Hour
            it[PreferencesKeys.SHOW_SECONDS] = showSeconds
        }
    }
}

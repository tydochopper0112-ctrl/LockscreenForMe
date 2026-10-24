package com.fold.lockscreen.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Top-level singleton delegate bound strictly to applicationContext
private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "lockscreen_prefs")

val Context.dataStore: DataStore<Preferences>
    get() = applicationContext.appPreferencesDataStore

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_WALLPAPER_PATH = stringPreferencesKey("selected_wallpaper_path")

        // Cover Screen (Outer) Layout
        val COVER_CLOCK_X = floatPreferencesKey("cover_clock_x")
        val COVER_CLOCK_Y = floatPreferencesKey("cover_clock_y")
        val COVER_CLOCK_SCALE = floatPreferencesKey("cover_clock_scale")
        val COVER_NOTIF_X = floatPreferencesKey("cover_notif_x")
        val COVER_NOTIF_Y = floatPreferencesKey("cover_notif_y")

        // Main Screen (Inner) Layout
        val MAIN_CLOCK_X = floatPreferencesKey("main_clock_x")
        val MAIN_CLOCK_Y = floatPreferencesKey("main_clock_y")
        val MAIN_CLOCK_SCALE = floatPreferencesKey("main_clock_scale")
        val MAIN_NOTIF_X = floatPreferencesKey("main_notif_x")
        val MAIN_NOTIF_Y = floatPreferencesKey("main_notif_y")
        val MAIN_TASKBAR_PADDING = intPreferencesKey("main_taskbar_padding")

        val NOTIFICATION_MODE = stringPreferencesKey("notification_mode")
        val DOUBLE_TAP_PACKAGE = stringPreferencesKey("double_tap_package")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
    }

    val settingsFlow: Flow<LockscreenSettings> = context.dataStore.data
        .catch { exception ->
            // Prevent any crash during DataStore read
            exception.printStackTrace()
            emit(emptyPreferences())
        }
        .map { preferences ->
            val wallpaperPath = preferences[PreferencesKeys.SELECTED_WALLPAPER_PATH] ?: "assets:wallpapers/BLACK.jpg"
            val notificationModeStr = preferences[PreferencesKeys.NOTIFICATION_MODE] ?: NotificationDisplayMode.COMPACT_LIST.name
            val notifMode = runCatching { NotificationDisplayMode.valueOf(notificationModeStr) }.getOrDefault(NotificationDisplayMode.COMPACT_LIST)

            val coverLayout = ScreenLayoutConfig(
                clockXPercent = preferences[PreferencesKeys.COVER_CLOCK_X] ?: 0.5f,
                clockYPercent = preferences[PreferencesKeys.COVER_CLOCK_Y] ?: 0.32f,
                clockScale = preferences[PreferencesKeys.COVER_CLOCK_SCALE] ?: 1.0f,
                notificationXPercent = preferences[PreferencesKeys.COVER_NOTIF_X] ?: 0.5f,
                notificationYPercent = preferences[PreferencesKeys.COVER_NOTIF_Y] ?: 0.62f,
                taskbarPaddingBottomDp = 32
            )

            val mainLayout = ScreenLayoutConfig(
                clockXPercent = preferences[PreferencesKeys.MAIN_CLOCK_X] ?: 0.5f,
                clockYPercent = preferences[PreferencesKeys.MAIN_CLOCK_Y] ?: 0.36f,
                clockScale = preferences[PreferencesKeys.MAIN_CLOCK_SCALE] ?: 1.15f,
                notificationXPercent = preferences[PreferencesKeys.MAIN_NOTIF_X] ?: 0.5f,
                notificationYPercent = preferences[PreferencesKeys.MAIN_NOTIF_Y] ?: 0.64f,
                taskbarPaddingBottomDp = preferences[PreferencesKeys.MAIN_TASKBAR_PADDING] ?: 64
            )

            LockscreenSettings(
                selectedWallpaperPath = wallpaperPath,
                coverLayout = coverLayout,
                mainLayout = mainLayout,
                notificationMode = notifMode,
                doubleTapPackage = preferences[PreferencesKeys.DOUBLE_TAP_PACKAGE] ?: "com.sec.android.app.clockpackage",
                showSeconds = preferences[PreferencesKeys.SHOW_SECONDS] ?: true
            )
        }

    suspend fun updateWallpaperPath(path: String) {
        runCatching {
            context.dataStore.edit { it[PreferencesKeys.SELECTED_WALLPAPER_PATH] = path }
        }
    }

    suspend fun updateCoverClock(x: Float, y: Float, scale: Float) {
        runCatching {
            context.dataStore.edit {
                it[PreferencesKeys.COVER_CLOCK_X] = x
                it[PreferencesKeys.COVER_CLOCK_Y] = y
                it[PreferencesKeys.COVER_CLOCK_SCALE] = scale
            }
        }
    }

    suspend fun updateCoverNotification(x: Float, y: Float) {
        runCatching {
            context.dataStore.edit {
                it[PreferencesKeys.COVER_NOTIF_X] = x
                it[PreferencesKeys.COVER_NOTIF_Y] = y
            }
        }
    }

    suspend fun updateMainClock(x: Float, y: Float, scale: Float) {
        runCatching {
            context.dataStore.edit {
                it[PreferencesKeys.MAIN_CLOCK_X] = x
                it[PreferencesKeys.MAIN_CLOCK_Y] = y
                it[PreferencesKeys.MAIN_CLOCK_SCALE] = scale
            }
        }
    }

    suspend fun updateMainNotification(x: Float, y: Float) {
        runCatching {
            context.dataStore.edit {
                it[PreferencesKeys.MAIN_NOTIF_X] = x
                it[PreferencesKeys.MAIN_NOTIF_Y] = y
            }
        }
    }

    suspend fun updateMainTaskbarPadding(paddingDp: Int) {
        runCatching {
            context.dataStore.edit {
                it[PreferencesKeys.MAIN_TASKBAR_PADDING] = paddingDp
            }
        }
    }

    suspend fun updateNotificationMode(mode: NotificationDisplayMode) {
        runCatching {
            context.dataStore.edit { it[PreferencesKeys.NOTIFICATION_MODE] = mode.name }
        }
    }

    suspend fun updateDoubleTapPackage(pkg: String) {
        runCatching {
            context.dataStore.edit { it[PreferencesKeys.DOUBLE_TAP_PACKAGE] = pkg }
        }
    }

    suspend fun updateShowSeconds(show: Boolean) {
        runCatching {
            context.dataStore.edit { it[PreferencesKeys.SHOW_SECONDS] = show }
        }
    }
}

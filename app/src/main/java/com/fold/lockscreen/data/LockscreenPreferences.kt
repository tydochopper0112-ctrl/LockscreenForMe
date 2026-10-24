package com.fold.lockscreen.data

enum class NotificationDisplayMode(val displayName: String) {
    HIDDEN("Ẩn"),
    COUNTER("Chỉ hiện số lượng"),
    COMPACT_LIST("Danh sách rút gọn (Tối đa 3)")
}

enum class FoldScreenType(val displayName: String) {
    COVER("Màn hình ngoài (Cover Screen)"),
    MAIN("Màn hình trong (Main Screen)")
}

data class ScreenLayoutConfig(
    val clockXPercent: Float = 0.5f,
    val clockYPercent: Float = 0.35f,
    val clockScale: Float = 1.0f,
    val notificationXPercent: Float = 0.5f,
    val notificationYPercent: Float = 0.65f,
    val taskbarPaddingBottomDp: Int = 48
)

data class LockscreenSettings(
    val selectedWallpaperPath: String = "assets:wallpapers/BLACK.jpg",
    val coverLayout: ScreenLayoutConfig = ScreenLayoutConfig(
        clockXPercent = 0.5f,
        clockYPercent = 0.32f,
        clockScale = 1.0f,
        notificationXPercent = 0.5f,
        notificationYPercent = 0.62f,
        taskbarPaddingBottomDp = 32
    ),
    val mainLayout: ScreenLayoutConfig = ScreenLayoutConfig(
        clockXPercent = 0.5f,
        clockYPercent = 0.36f,
        clockScale = 1.15f,
        notificationXPercent = 0.5f,
        notificationYPercent = 0.64f,
        taskbarPaddingBottomDp = 64
    ),
    val notificationMode: NotificationDisplayMode = NotificationDisplayMode.COMPACT_LIST,
    val doubleTapPackage: String = "com.sec.android.app.clockpackage",
    val showSeconds: Boolean = true
)

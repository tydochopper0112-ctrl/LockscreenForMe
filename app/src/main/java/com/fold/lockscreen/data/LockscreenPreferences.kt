package com.fold.lockscreen.data

enum class ClockStyle(val displayName: String) {
    DIGITAL_MINIMAL("Digital Minimal"),
    DOT_MATRIX("Dot-Matrix"),
    ANALOG("Analog"),
    FLIP_CLOCK("Flip Clock")
}

enum class FontStyle(val displayName: String) {
    MONOSPACE("Monospace"),
    SANS_SERIF("Sans-Serif"),
    SERIF("Serif"),
    DOT_MATRIX("Dot-Matrix (Custom)")
}

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
    val notificationXPercent: Float = 0.5f,
    val notificationYPercent: Float = 0.65f,
    val taskbarPaddingBottomDp: Int = 48
)

data class LockscreenSettings(
    val clockStyle: ClockStyle = ClockStyle.DIGITAL_MINIMAL,
    val fontStyle: FontStyle = FontStyle.MONOSPACE,
    val clockSizeScale: Float = 1.0f,
    val clockColor: Long = 0xFFFFFFFF,
    val accentColor: Long = 0xFF00E5FF,
    val backgroundColor: Long = 0xFF000000,
    val coverLayout: ScreenLayoutConfig = ScreenLayoutConfig(
        clockXPercent = 0.5f,
        clockYPercent = 0.32f,
        notificationXPercent = 0.5f,
        notificationYPercent = 0.60f,
        taskbarPaddingBottomDp = 32
    ),
    val mainLayout: ScreenLayoutConfig = ScreenLayoutConfig(
        clockXPercent = 0.5f,
        clockYPercent = 0.36f,
        notificationXPercent = 0.5f,
        notificationYPercent = 0.64f,
        taskbarPaddingBottomDp = 64 // Space reserved for Samsung One UI Taskbar
    ),
    val notificationMode: NotificationDisplayMode = NotificationDisplayMode.COMPACT_LIST,
    val doubleTapPackage: String = "com.sec.android.app.clockpackage",
    val showSeconds: Boolean = true,
    val is24Hour: Boolean = true
)

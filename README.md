# Lockscreen for me 📱✨
### Ứng dụng hình nền động & tuỳ biến màn hình khoá tối ưu chuyên biệt cho Samsung Galaxy Z Fold 7
*(Hỗ trợ Android 10+ / minSdk 29, targetSdk 35 | Kotlin & Jetpack Compose)*

---

## 🌟 Điểm nổi bật & Tính năng chính

### 1. ⚡ Lưu trữ cài đặt & Đồng bộ thời gian thực (Jetpack DataStore)
- Toàn bộ tuỳ chỉnh (kiểu đồng hồ, font chữ, màu sắc, toạ độ màn hình gập) được quản lý qua **Jetpack Preferences DataStore**.
- `LockscreenWallpaperService.Engine` đăng ký lắng nghe trực tiếp thông qua **Kotlin Coroutines Flow**: Khi người dùng thay đổi bất kỳ cài đặt nào trong app (kéo thanh trượt, đổi font, đổi màu), hình nền Canvas lập tức vẽ lại tức thì mà **không cần người dùng phải đặt lại hình nền**.

### 2. 🕰️ Tuỳ chỉnh Đồng hồ & Font chữ Canvas
- **4 Kiểu đồng hồ Canvas:**
  1. **Digital Minimal:** Hiện đại, thanh lịch, tinh giản chuẩn flagship với ngày tháng tiếng Việt/quốc tế.
  2. **Dot-Matrix:** Mô phỏng ma trận chấm LED/VFD cổ điển với các chấm phát sáng (lit dots) và chấm mờ (unlit dots).
  3. **Analog:** Mặt đồng hồ kim AMOLED tối giản, kim giờ, kim phút, kim giây lướt mượt mà, vạch chia giờ và tâm kim tinh xảo.
  4. **Flip Clock:** Phong cách đồng hồ lật cơ học (Split-Flap) với rãnh chia đôi bản lật, hiệu ứng bóng đổ và chuyển màu gradient.
- **4 Kiểu Font chữ:**
  1. `Monospace`
  2. `Sans-Serif`
  3. `Serif`
  4. `Dot-Matrix` (Tích hợp bộ ký tự 5x7 ma trận tự vẽ trực tiếp trên Canvas).
- **Thanh trượt & Tuỳ biến màu sắc:**
  - Thanh trượt chỉnh tỷ lệ kích thước đồng hồ từ 60% đến 180%.
  - Bảng chọn màu sắc: Màu chữ đồng hồ, màu điểm nhấn (Accent), màu nền (Mặc định chuẩn đen thuần **AMOLED #000000** giúp tiết kiệm pin tối đa cho màn hình Dynamic AMOLED 2X của Z Fold 7).
  - Tuỳ chọn định dạng 24 giờ và bật/tắt kim giây.

### 3. 🔔 Hệ thống Thông báo (NotificationListenerService)
- Tích hợp `LockscreenNotificationListenerService` đọc các thông báo hoạt động từ hệ thống.
- Trong màn hình Cài đặt, có nút kiểm tra trạng thái và cấp quyền đọc thông báo (dẫn trực tiếp tới `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`).
- **3 Chế độ hiển thị trên Canvas:**
  - **Ẩn (Hidden):** Không hiển thị thông báo.
  - **Chỉ hiện số lượng (Counter):** Hiển thị viên nang đếm số thông báo mới (ví dụ: `● 3 thông báo mới`).
  - **Danh sách rút gọn (Compact List):** Hiển thị thẻ bo tròn tối đa 3 thông báo mới nhất kèm Tên ứng dụng (App Badge) + Tiêu đề/Nội dung rút gọn.

### 4. 📐 Đa nhiệm thông minh cho Màn hình gập (Foldable Dual-Layout)
- **Tự động nhận diện màn hình theo tỷ lệ khung hình tại `onSurfaceChanged`:**
  - `width / height < 0.6`: Tự động kích hoạt cấu hình **Cover Screen (Màn hình ngoài)** của Z Fold 7 (tỷ lệ xấp xỉ 22:9).
  - `width / height > 0.7`: Tự động kích hoạt cấu hình **Main Screen (Màn hình trong gập)** của Z Fold 7 (tỷ lệ xấp xỉ 6:5).
- **Toạ độ X/Y độc lập:**
  - Cho phép người dùng chỉnh toạ độ X và Y riêng biệt cho màn hình ngoài và màn hình trong.
  - Có thanh trượt **Bảo vệ Taskbar Samsung One UI**: Chừa khoảng trống tuỳ chỉnh (mặc định 64dp) ở chân màn hình trong để các widget hay thanh Taskbar đa nhiệm không đè lên thông báo.
- **Cử chỉ Chạm đúp (Double Tap):**
  - Bắt sự kiện `onTouchEvent` với `GestureDetector.onDoubleTap`.
  - Khi chạm đúp vào vùng đồng hồ hoặc danh sách thông báo, điện thoại rung phản hồi haptic và lập tức khởi động ứng dụng chỉ định (mặc định: Đồng hồ/Báo thức Samsung `com.sec.android.app.clockpackage`, Lịch, hoặc bất kỳ app nào người dùng chọn).

### 5. 🚀 Đóng gói tự động (GitHub Actions)
- File `.github/workflows/release.yml` cấu hình sẵn:
  - Tự động kích hoạt khi gắn tag `v*` (ví dụ: `v1.0.0`).
  - Tự động dựng bản build APK (`assembleRelease` & `assembleDebug`) trên môi trường Java 17.
  - Tự động tạo GitHub Release và đính kèm file APK sẵn sàng cài đặt.

---

## 🏗️ Cấu trúc dự án

```
Lockscreen/
├── .github/
│   └── workflows/
│       └── release.yml                 # CI/CD tự động build & release APK
├── gradle/
│   └── libs.versions.toml              # Version Catalog quản lý thư viện
├── app/
│   ├── build.gradle.kts                # Cấu hình Android SDK 35, MinSdk 29, Compose
│   └── src/main/
│       ├── AndroidManifest.xml         # Khai báo WallpaperService, NotificationListener
│       ├── res/
│       │   ├── xml/lockscreen_wallpaper.xml
│       │   └── values/{strings,colors,themes}.xml
│       └── java/com/fold/lockscreen/
│           ├── LockscreenApp.kt        # Application class khởi tạo singleton
│           ├── data/
│           │   ├── LockscreenPreferences.kt # Data models & cấu hình toạ độ
│           │   ├── PreferencesManager.kt    # Jetpack DataStore Flow quản lý reactive prefs
│           │   └── NotificationModel.kt     # Repository thông báo
│           ├── service/
│           │   ├── LockscreenWallpaperService.kt # Engine vẽ Canvas & xử lý gập mở
│           │   └── LockscreenNotificationListenerService.kt # Bridge đọc thông báo
│           ├── canvas/
│           │   ├── ClockRenderer.kt         # Interface vẽ đồng hồ & tính bounds
│           │   ├── DigitalClockRenderer.kt  # Kiểu Digital Minimal
│           │   ├── DotMatrixClockRenderer.kt# Kiểu Dot-Matrix retro
│           │   ├── DotMatrixFont.kt         # Bộ ký tự 5x7 dot matrix
│           │   ├── AnalogClockRenderer.kt   # Kiểu đồng hồ kim AMOLED
│           │   ├── FlipClockRenderer.kt     # Kiểu đồng hồ lật Split-Flap
│           │   └── NotificationRenderer.kt  # Vẽ thẻ thông báo & counter
│           └── ui/
│               ├── MainActivity.kt          # Host Compose UI & kiểm tra quyền
│               ├── theme/                   # Material 3 AMOLED Dark theme
│               ├── components/
│               │   ├── ColorPickerRow.kt    # Chọn màu sắc preset
│               │   └── PreviewCanvas.kt     # Giả lập màn hình ngoài & màn hình trong
│               └── screens/
│                   ├── MainSettingsScreen.kt# Giao diện chính 4 Tabs + nút Đặt hình nền
│                   ├── ClockSettingsTab.kt  # Cài đặt đồng hồ, font, kích thước, màu
│                   ├── FoldPositioningTab.kt# Cài đặt toạ độ X/Y Cover vs Main Screen
│                   └── NotificationSettingsTab.kt # Quản lý quyền & chế độ thông báo
```

---

## 🛠️ Hướng dẫn Biên dịch và Sử dụng

### 1. Build bằng Android Studio hoặc Gradle
```bash
# Cấp quyền thực thi cho gradlew (trên Linux/macOS)
chmod +x gradlew

# Build bản phát hành APK
./gradlew assembleRelease

# File APK tạo ra tại:
app/build/outputs/apk/release/app-release.apk
```

### 2. Kích hoạt Hình nền động trên Samsung Galaxy Z Fold 7
1. Cài đặt file APK vào máy.
2. Mở ứng dụng **Lockscreen for me**.
3. Cấp quyền đọc thông báo tại tab **Thông báo**.
4. Chọn kiểu đồng hồ, font chữ và toạ độ mong muốn.
5. Nhấn nút **"Đặt làm Hình nền Lockscreen"** ở góc dưới màn hình.
6. Chọn áp dụng cho **Màn hình khoá** (hoặc cả Màn hình khoá và Màn hình chính).
7. Thử gập hoặc mở máy: Hình nền sẽ tự động nhận diện tỷ lệ khung hình và hiển thị đúng bố cục bạn đã cấu hình cho Cover Screen và Main Screen!

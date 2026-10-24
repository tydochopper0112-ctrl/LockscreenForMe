# Lockscreen for me (v1.1.2) 📱✨
### Ứng dụng hình nền động & tuỳ biến màn hình khoá tối ưu chuyên biệt cho Samsung Galaxy Z Fold 7
*(Hỗ trợ Android 10+ / minSdk 29, targetSdk 35 | Kotlin & Jetpack Compose)*

---

## 🌟 Tính năng & Cải tiến trên Phiên bản v1.1.2

### 1. 🛡️ Khắc phục triệt để lỗi Crash khi khởi động (Zero-Crash Architecture)
- **Xóa bỏ thuộc tính XML lỗi:** Đã gỡ bỏ `android:dataExtractionRules` trỏ nhầm sang XML Wallpaper trong `AndroidManifest.xml` (nguyên nhân chính khiến hệ thống Android 12+ / Z Fold 7 văng ứng dụng ngay khi chạm biểu tượng).
- **Chuẩn hóa Theme Material:** Chuyển `themes.xml` sang chuẩn `android:Theme.Material.NoActionBar` tương thích 100% với `ComponentActivity` của Jetpack Compose và cơ chế Edge-to-Edge của Android 15.
- **Top-level Singleton DataStore:** Cố định `Context.dataStore` duy nhất ở cấp độ `applicationContext`, triệt tiêu hoàn toàn ngoại lệ `IllegalStateException: Multiple DataStores active`.
- **Tắt R8 Code Shrinking:** Đảm bảo `isMinifyEnabled = false` và `isShrinkResources = false` trong `app/build.gradle.kts` để không bị xóa nhầm class.
- **Font chữ hệ thống an toàn 100%:** Sử dụng toàn bộ `Typeface.SANS_SERIF`, `FontFamily.SansSerif` và tự vẽ bằng Canvas để không bao giờ bị `Resources$NotFoundException`.

### 2. 🚨 Màn hình Báo cáo Sự cố Toàn cục (Global Crash Screen)
- Tích hợp `Thread.setDefaultUncaughtExceptionHandler` chạy trên tiến trình riêng `:crash`.
- Nếu có bất kỳ lỗi đột ngột nào xảy ra, ứng dụng sẽ không bị văng mà tự động mở màn hình **Crash Handler**:
  - Hiển thị chi tiết lỗi, dòng code gây lỗi và toàn bộ **Stack Trace**.
  - Nút **"Sao chép lỗi"** để bạn dễ dàng gửi báo cáo.
  - Nút **"Khởi động lại"** giúp mở lại ứng dụng tức thì.

### 3. 🖼️ Kho ảnh nền có sẵn & Chọn ảnh từ máy (Center-Crop)
- **Kho ảnh nền độc quyền tích hợp:** Gồm `BLACK.jpg`, `Don't 2.jpg`, `Don't 3.jpg`, `Split-Panel-LF.jpg`.
- **Thanh cuộn ngang Thumbnail Gallery:** Chọn hình nền trực tiếp bằng một chạm.
- **Chọn ảnh từ thư viện máy:** Hỗ trợ `PickVisualMedia` để chọn ảnh từ điện thoại Z Fold 7.
- **Center-Crop:** Tự động căn giữa, lấp đầy cả màn hình ngoài (22:9) và màn hình trong (6:5) mà không méo ảnh.

### 4. 🖐️ Khung Xem Trước Kéo-Thả Trực Quan (WYSIWYG Drag & Drop)
- Chạm & Kéo vị trí Đồng hồ hoặc Thông báo.
- Dùng 2 ngón tay phóng to / thu nhỏ trực tiếp (Pinch-to-zoom).
- Tự động hít vào tâm màn hình (Snap-to-center guidelines 50%).

### 5. 🕰️ Kiểu Đồng Hồ Độc Quyền "Life is to short Live It"
- Tái tạo chính xác duy nhất mẫu đồng hồ nghệ thuật:
  - Nét đứng của chữ **"L"** chạy dài xuyên qua tâm kim.
  - Chữ **"ife"** thanh mảnh, tinh tế.
  - Phụ đề **"is to short"** trắng sáng và hành động **"Live It"** đỏ rực rỡ.
  - Kim giờ, phút, giây chuyển động thời gian thực.

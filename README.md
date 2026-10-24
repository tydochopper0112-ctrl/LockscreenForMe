# Lockscreen for me (v1.1.0) 📱✨
### Ứng dụng hình nền động & tuỳ biến màn hình khoá tối ưu chuyên biệt cho Samsung Galaxy Z Fold 7
*(Hỗ trợ Android 10+ / minSdk 29, targetSdk 35 | Kotlin & Jetpack Compose)*

---

## 🌟 Tính năng mới nổi bật trên Phiên bản v1.1.0

### 1. 🖼️ Kho ảnh nền có sẵn & Chọn ảnh từ thư viện máy
- **Kho ảnh nền độc quyền tích hợp:** Đã chuyển toàn bộ ảnh nền (`BLACK.jpg`, `Don't 2.jpg`, `Don't 3.jpg`, `Split-Panel-LF.jpg`) vào `app/src/main/assets/wallpapers/`.
- **Thanh cuộn ngang Thumbnail Gallery:** Hiển thị trực quan hình thu nhỏ của các hình nền để bạn bấm chọn tức thì.
- **Nút chọn ảnh từ thư viện máy:** Tích hợp `ActivityResultContracts.PickVisualMedia` cho phép bạn chọn ảnh bất kỳ từ điện thoại Z Fold 7.
- **Center-Crop thông minh:** Trong `WallpaperService.Engine`, thuật toán Center-Crop tự động căn giữa và lấp đầy cả **Cover Screen (22:9)** và **Main Screen (6:5)** của Z Fold 7 mà không làm méo hay biến dạng hình ảnh.

### 2. 🖐️ Khung Xem Trước Kéo-Thả Trực Quan (WYSIWYG Drag & Drop)
- **Xóa bỏ hoàn toàn thanh trượt tọa độ:** Không còn các thanh trượt hay ô nhập số khó căn chỉnh.
- **Khung xem trước tương tác thời gian thực:**
  - Nút chuyển đổi nhanh giữa **Màn hình ngoài (Cover Screen 22:9)** và **Màn hình trong (Main Screen 6:5)**.
  - **Chạm & Kéo (Drag & Drop):** Dùng 1 ngón tay chạm và di chuyển vị trí của Đồng hồ hoặc Thẻ thông báo đến bất kỳ toạ độ nào trên màn hình.
  - **Phóng to / Thu nhỏ (Pinch-to-zoom):** Dùng 2 ngón tay kéo dãn để tùy chỉnh kích thước đồng hồ trực tiếp.
  - **Đường lưới căn giữa tự động (Snap-to-center):** Tự động hít vào tâm dọc (50%) và tâm ngang (50%) kèm vạch hiển thị Cyan khi kéo gần tới giữa màn hình.
  - Toạ độ được chuẩn hoá lưu trữ theo tỷ lệ phần trăm (0.0f đến 1.0f) trong Jetpack DataStore, đảm bảo độ chính xác 100% giữa khung xem trước và hình nền thực tế.

### 3. 🕰️ Kiểu Đồng Hồ Độc Quyền "Life is to short Live It"
- Tái tạo chính xác duy nhất mẫu đồng hồ từ file thiết kế của bạn:
  - Chữ **"Life"**: Nét đứng của chữ **"L"** chạy dài xuyên qua tâm kim đồng hồ; cụm chữ **"ife"** mang font sans-serif hình học thanh mảnh, tinh tế.
  - Dòng chữ phụ **"is to short"** màu trắng tinh khiết đặt ngay dưới tâm đồng hồ.
  - Dòng chữ hành động **"Live It"** màu đỏ nổi bật (`#FF1122`), in đậm bên dưới.
  - Kim đồng hồ chuyển động thời gian thực với kim giờ trắng, kim phút trắng, kim giây đỏ rực rỡ kèm đuôi kim dài đối xứng tạo điểm nhấn nghệ thuật ấn tượng.
  - Tâm đồng hồ dạng vòng tròn viền kép đen - trắng tinh xảo.

---

## 🏗️ Cấu trúc dự án v1.1.0

```
Lockscreen/
├── .github/workflows/
│   └── release.yml                             # Tự động đóng gói APK & tạo GitHub Release
├── app/
│   ├── build.gradle.kts                        # minSdk 29, targetSdk 35, versionCode 2, versionName 1.1.0
│   └── src/main/
│       ├── assets/
│       │   ├── wallpapers/                     # Kho ảnh nền tích hợp
│       │   └── clock/                          # Ảnh mẫu đồng hồ tham chiếu
│       ├── AndroidManifest.xml                 # Khai báo WallpaperService & NotificationListenerService
│       └── java/com/fold/lockscreen/
│           ├── LockscreenApp.kt                # Application class
│           ├── data/
│           │   ├── LockscreenPreferences.kt    # Cấu hình toạ độ & tỷ lệ
│           │   ├── PreferencesManager.kt       # Quản lý DataStore thời gian thực
│           │   ├── NotificationModel.kt        # Repository thông báo
│           │   └── WallpaperHelper.kt          # Xử lý Center-Crop & nạp ảnh nền
│           ├── service/
│           │   ├── LockscreenWallpaperService.kt           # Engine hình nền Canvas Z Fold 7
│           │   └── LockscreenNotificationListenerService.kt# Cầu nối đọc thông báo
│           ├── canvas/
│           │   ├── ClockRenderer.kt            # Interface vẽ đồng hồ
│           │   ├── LifeClockRenderer.kt        # Đồng hồ "Life is to short Live It"
│           │   └── NotificationRenderer.kt     # Hiển thị thẻ thông báo & Counter
│           └── ui/
│               ├── MainActivity.kt             # Activity chính
│               ├── components/
│               │   ├── InteractiveWysiwygPreview.kt # Khung kéo thả trực quan WYSIWYG
│               │   └── WallpaperGalleryRow.kt       # Thư viện cuộn chọn ảnh nền
│               └── screens/
│                   └── MainSettingsScreen.kt   # Giao diện chính đồng nhất
```

---

## 🛠️ Hướng dẫn Sử dụng
1. Mở ứng dụng **Lockscreen for me**.
2. Chọn hình nền trong **Kho ảnh nền** hoặc bấm **"Chọn ảnh từ máy"** để tải ảnh yêu thích từ điện thoại.
3. Chạm và kéo đồng hồ hoặc thông báo trực tiếp trên **Khung xem trước**. Dùng 2 ngón tay thu phóng kích thước tùy thích.
4. Chuyển đổi giữa tab **Màn ngoài (Cover)** và **Màn trong (Main)** để căn chỉnh độc lập cho từng màn hình gập của Z Fold 7.
5. Bấm **"Đặt làm Hình nền Lockscreen"** để áp dụng.

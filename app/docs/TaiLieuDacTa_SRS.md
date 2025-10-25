# Support Meditation – Tài liệu đặc tả hệ thống (SRS)

## 1. Giới thiệu

Ứng dụng **Support Meditation** hỗ trợ người dùng ngồi thiền có hướng dẫn âm thanh theo cấu hình tùy chỉnh.

Hệ thống cho phép tạo nhiều cấu hình thiền (`MeConfig`), mỗi cấu hình gồm nhiều giai đoạn (`MeStage`). Trong mỗi giai đoạn, ứng dụng phát ngẫu nhiên một âm thanh từ danh sách đã chọn, lặp lại theo chu kỳ thời gian định sẵn.

### Lưu ý quan trọng

- **Chỉ sử dụng đơn vị phút**
- **Quản lý file âm thanh trực tiếp qua thư mục**, không dùng cơ sở dữ liệu
- **Giữ màn hình luôn bật** trong suốt phiên thiền, tự động tắt khi kết thúc/dừng

---

## 2. Cấu trúc dữ liệu chính

### 2.1. MeConfig

Đại diện cho một buổi thiền.

**Thuộc tính:**
- `name`: tên buổi thiền (chuỗi, bắt buộc)
- `totalMinutes`: tổng thời gian thiền (số nguyên ≥1, ≤180)
- `stages`: danh sách các `MeStage` (thứ tự quan trọng)

**Ràng buộc:**
- Tổng `minutes` của tất cả `MeStage` phải ≤ `totalMinutes`
- Nếu vượt → hệ thống không cho lưu và hiển thị cảnh báo
- `totalMinutes` giới hạn tối đa 180 phút (3 giờ)

---

### 2.2. MeStage

Một giai đoạn trong buổi thiền.

**Thuộc tính:**
- `minutes`: thời lượng giai đoạn (số nguyên ≥1, ≤180)
- `repeatMinutes`: chu kỳ lặp âm thanh (0 = chỉ phát 1 lần, ≤60)
- `sounds`: danh sách tên file âm thanh (chỉ lưu tên, không lưu đường dẫn)

**Hành vi:**
- Khi vào giai đoạn, chọn ngẫu nhiên 1 file trong `sounds` để phát
- Nếu `repeatMinutes > 0`, cứ sau mỗi `repeatMinutes` phút lại phát một âm thanh ngẫu nhiên mới

**Ví dụ:**
```json
{
  "minutes": 8,
  "repeatMinutes": 2,
  "sounds": ["bell.mp3", "rain.wav"]
}
```

---

### 2.3. StageSound

Là tên file (ví dụ: `"ocean.wav"`).

**Quy tắc đặt tên:**
- Chỉ chứa ký tự an toàn: chữ cái (`a–z`, `A–Z`), số (`0–9`), dấu gạch dưới `_`, gạch nối `-`, và dấu chấm `.`
- Mọi tên file được tự động làm sạch trước khi lưu hoặc đổi tên
- Thay thế ký tự không hợp lệ bằng `_`

---

## 3. Cấu trúc ứng dụng

### 3.1. Màn hình chính (ConfigList)

- Hiển thị danh sách `MeConfig`
- **Chức năng:** tạo mới, chỉnh sửa, xóa, bắt đầu thiền

### 3.2. Cài đặt (Settings)

- **Mức độ rung:** Tắt / Nhẹ / Mạnh
- **Âm thanh:** Bật / Tắt
- **Mức giảm sáng màn hình:** điều chỉnh % (0-100%, mặc định 30%)

### 3.3. Thư viện âm thanh (Sound Library)

- Truy cập qua menu chính (không gắn với `MeStage`)
- Liệt kê file từ thư mục: `/sdcard/Music/SupportMeditation/`
- **Không lưu danh sách** vào bất kỳ nơi nào — quét lại mỗi lần mở

---

## 4. Màn hình chỉnh sửa

### 4.1. MeConfig Editor

- Nhập `name`, `totalMinutes`
- Hiển thị danh sách `MeStage` dưới dạng **card list** (không dùng tree view)
- Cho phép thêm/sửa/xóa `MeStage` trực tiếp
- Kiểm tra tổng thời gian trước khi lưu
- Hiển thị cảnh báo nếu tổng thời gian các stage vượt quá `totalMinutes`

### 4.2. MeStage Editor

- Nhập `minutes` (≥1, ≤180), `repeatMinutes` (≥0, ≤60)
- Nút **"Chọn âm thanh"** → mở dialog chọn file riêng (xem mục 6)
- Hiển thị danh sách file đã chọn

---

## 5. Màn hình quản lý file âm thanh (Sound Library)

### Thư mục làm việc
```
/sdcard/Music/SupportMeditation/
```

### Hành vi

- Mỗi lần mở → quét lại thư mục, không dùng cache
- Chỉ hiển thị file có đuôi: `.mp3`, `.wav`, `.ogg`, `.m4a`

### Tính năng

#### ➕ Thêm file

1. Nhấn **"Thêm"** → mở trình chọn file hệ thống (Storage Access Framework)
2. Người dùng chọn một hoặc nhiều file
3. Với mỗi file được chọn:
   - Hiển thị hộp thoại **"Đặt tên file"** với:
     - Giá trị mặc định: tên gốc đã được làm sạch
     - Cho phép người dùng sửa lại tên
   - Hệ thống làm sạch tên mới (thay ký tự không hợp lệ bằng `_`)
   - Copy file vào thư mục làm việc với tên đã chỉnh
   - **Nếu trùng tên:** thêm số vào cuối (ví dụ: `file.mp3`, `file_1.mp3`, `file_2.mp3`)

#### 🗑️ Xoá file

1. Chọn một hoặc nhiều file → nhấn **"Xoá"**
2. Hiển thị xác nhận: **"Xoá [N] file? Hành động này không thể hoàn tác."**
3. Xoá vĩnh viễn khỏi thư mục làm việc
4. **Kiểm tra:** Nếu file đang được sử dụng trong bất kỳ config nào → cảnh báo trước khi xóa

⚠️ **Lưu ý:** Màn hình này không trả kết quả về `MeStage` — chỉ quản lý thư viện dùng chung.

---

## 6. Dialog chọn âm thanh cho MeStage

**Kích hoạt:** từ MeStage Editor → nhấn **"Chọn âm thanh"**

### Hành vi

- Quét trực tiếp thư mục làm việc (giống Sound Library)
- Tự động tích chọn các file đã có trong `sounds` của MeStage hiện tại
- Không cho phép chọn trùng: mỗi file chỉ được chọn một lần trong cùng MeStage
- Giao diện hiển thị rõ ràng: checkbox + tên file đã làm sạch
- **Nếu không có file nào:** Hiển thị thông báo hướng dẫn thêm file qua Sound Library

**Khi nhấn "Xong":**
- Cập nhật `MeStage.sounds` theo danh sách được chọn
- Không thay đổi nội dung thư mục

---

## 7. Luồng hoạt động

### 7.1. Lần đầu mở ứng dụng

1. Tạo thư mục `/sdcard/Music/SupportMeditation/` nếu chưa tồn tại
2. Copy file âm thanh mặc định từ `res/raw` → thư mục làm việc
3. Yêu cầu quyền truy cập storage nếu cần (Android 11+)

### 7.2. Người dùng tạo cấu hình

1. Vào **Thư viện âm thanh** để thêm/xoá file
2. Vào **ConfigList** để tạo cấu hình mới
3. Trong **MeStage Editor**, dùng dialog chọn file để gán âm thanh

### 7.3. Khi thiền

1. **Kiểm tra file:** Trước khi bắt đầu, kiểm tra tất cả file âm thanh có tồn tại không
   - Nếu thiếu file → hiển thị cảnh báo: **"Một số file âm thanh không tồn tại. Tiếp tục?"**
2. **Bật chế độ giữ màn hình:** Sử dụng `FLAG_KEEP_SCREEN_ON` để màn hình luôn bật
3. Phát âm thanh ngẫu nhiên theo `MeStage`
4. Lặp lại theo `repeatMinutes`
5. Áp dụng độ giảm sáng từ Settings (nếu có)

### 7.4. Khi kết thúc/dừng thiền

1. **Tắt chế độ giữ màn hình:** Xóa `FLAG_KEEP_SCREEN_ON`
2. Hiển thị màn hình hoàn thành với:
   - Tổng thời gian thiền
   - Số giai đoạn đã hoàn thành
   - Nút **"Hoàn tất"** quay về ConfigList
3. Phát âm thanh/rung đặc biệt (nếu bật)

---

## 8. Yêu cầu kỹ thuật

### 8.1. Nền tảng

- **Android:** API ≥ 26 (Android 8.0)
- **Ngôn ngữ:** java (ưu tiên)

### 8.2. Storage

- **Thư mục âm thanh:** `/sdcard/Music/SupportMeditation/`
- **Lưu trữ cấu hình:** File JSON (internal storage hoặc external storage)
- **Quản lý file:** Storage Access Framework (Android 10+) hoặc Scoped Storage

### 8.3. Quyền cần thiết

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### 8.4. Tính năng đặc biệt

#### Giữ màn hình bật



#### Xử lý phát âm thanh


- **SoundPool** 

### 8.5. Làm sạch tên file

Thay thế ký tự không hợp lệ:
```
: / \ : * ? " < > | → _
Khoảng trắng → _
```

Regex: `[^a-zA-Z0-9._-]` → `_`

---

## 9. Xử lý Edge Cases

### 9.1. File âm thanh

| Tình huống | Xử lý |
|------------|-------|
| File bị thiếu khi thiền | Cảnh báo trước khi bắt đầu, cho phép tiếp tục |
| Không có file nào trong thư mục | Hiển thị hướng dẫn thêm file |
| File đang dùng bị xóa | Cảnh báo trước khi xóa |
| Trùng tên khi thêm | Tự động thêm số: `file.mp3`, `file_1.mp3` |

### 9.2. Trong lúc thiền

| Tình huống | Xử lý |
|------------|-------|
| Tắt màn hình | Giữ timer chạy, tiếp tục phát âm thanh |


| Nhấn nút Back | Hiển thị xác nhận: **"Dừng thiền?"** |


### 9.3. Quyền

- Nếu không có quyền → hướng dẫn cấp quyền với ảnh chụp màn hình
- Test trên Android 11+ với Scoped Storage

---

## 10. Ghi chú phát triển

### 10.1. Giao diện

- **Tối giản, không gây phân tâm**
- Màu sắc: nền tối/trung tính, chữ sáng
- Font size: ≥16sp cho dễ đọc

### 10.2. Xử lý lỗi

- Luôn có try-catch cho thao tác file I/O
- Log lỗi để debug, hiển thị thông báo thân thiện cho user
- Không crash app trong bất kỳ trường hợp nào

### 10.3. Hiệu năng

- Không load toàn bộ nội dung file — chỉ đọc tên
- Giải phóng MediaPlayer sau khi dùng xong
- Sử dụng coroutines cho thao tác file không đồng bộ

---

## 11. Roadmap

### Phase 1 (MVP)
- ✅ Tạo/chỉnh sửa config
- ✅ Phát âm thanh theo stage
- ✅ Giữ màn hình bật khi thiền
- ✅ Quản lý file âm thanh cơ bản


---

## 12. Công nghệ sử dụng
- 
- java
- android jetpack compose
- âm thanh SoundPool
- không sử dụng service mà chạy trực tiếp với màn hình luôn bật
- Storage: JSON serialization
- khi start thiền khi vào bắt đầu, tôi muốn cho vòng lặp chạy mỗi 10 giây để check hiện tại đang ở mestage nào, lần gần nhất playsound lúc nào, đã để lúc playsound tiếp chưa , đã kết thúc thiền chưa. bạn thấy thế nào
- sử dụng công nghệ đơn giản để dễ bảo trì code
- class, function nào có thể dùng nhiều lần thì nên tách ra để tái sử dụng
## Phụ lục A: Ví dụ JSON Config

```json
{
  "name": "Thiền buổi sáng",
  "totalMinutes": 30,
  "stages": [
    {
      "minutes": 5,
      "repeatMinutes": 0,
      "sounds": ["bell_start.mp3"]
    },
    {
      "minutes": 20,
      "repeatMinutes": 5,
      "sounds": ["bowl.wav", "chime.mp3"]
    },
    {
      "minutes": 5,
      "repeatMinutes": 0,
      "sounds": ["bell_end.mp3"]
    }
  ]
}
```

---

**Version:** 2.0  
**Last Updated:** 2025-10-24  
**Author:** Support Meditation Team
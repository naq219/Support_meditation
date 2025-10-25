# Support Meditation – Tài liệu Thiết kế Giao diện Người dùng (UI/UX Specification)

## Nguyên tắc thiết kế chung
- Tối giản, không gây phân tâm — phù hợp với ngữ cảnh thiền định.
- Màu sắc: nền trung tính hoặc tối, chữ sáng, không hiệu ứng chớp nháy.
- Phông chữ: dễ đọc, kích thước đủ lớn cho thiết bị di động.
- Tương tác: ít bước, trực quan, phản hồi rõ ràng.

## 1. Màn hình chính – ConfigList (Home)
### Mục đích
Hiển thị danh sách buổi thiền đã tạo, cho phép bắt đầu, tạo mới, hoặc truy cập thư viện/cài đặt.

### Thành phần giao diện
- **Tiêu đề**: “Buổi thiền của bạn”
- **Danh sách cuộn dọc các buổi thiền dưới dạng card**:
  - Mỗi card hiển thị:
    - Tên buổi thiền
    - Tổng thời gian (ví dụ: “30 phút”)
    - Số giai đoạn (ví dụ: “3 giai đoạn”)
- **Nút nổi (FAB)** ở góc dưới phải: “+” (Tạo mới)
- **Menu trên thanh tiêu đề** (3 chấm):
  - “Thư viện âm thanh”
  - “Cài đặt”

### Hành vi
- Nhấn vào card → bắt đầu thiền ngay.
- Nhấn giữ card → menu ngữ cảnh: “Chỉnh sửa”, “Xoá”.
- Nhấn FAB → mở màn hình MeConfig Editor (chế độ tạo mới).
- Nhấn menu → điều hướng đến Sound Library hoặc Settings.

## 2. Màn hình tạo/chỉnh sửa cấu hình – MeConfig Editor
### Mục đích
Tạo hoặc chỉnh sửa một buổi thiền (MeConfig), bao gồm quản lý các giai đoạn (MeStage).

### Thành phần giao diện
- **Trường nhập**:
  - “Tên buổi thiền” (EditText, bắt buộc)
  - “Tổng thời gian (phút)” (Number input, ≥1)
- **Danh sách giai đoạn dưới dạng card**:
  - Mỗi card hiển thị:
    - Tên gợi ý (nếu có) hoặc “Giai đoạn không tên”
    - Thời lượng: “X phút”
    - Chu kỳ lặp: “Lặp mỗi Y phút” hoặc “Chỉ phát 1 lần”
    - Số âm thanh: “N âm thanh”
  - Mỗi card có nút **✏️ (Sửa)** và **🗑️ (Xoá)** ở góc phải.
- **Nút “+ Thêm giai đoạn”** dưới cùng danh sách.
- **Nút “Lưu”** trên thanh công cụ.

### Hành vi
- Nếu tổng thời gian các giai đoạn vượt quá totalMinutes:
  - Hiển thị thanh cảnh báo đỏ: “Tổng thời gian giai đoạn vượt quá [totalMinutes] phút”
  - Vô hiệu hóa nút “Lưu”.
- Nhấn “+ Thêm giai đoạn” → mở MeStage Editor (chế độ tạo mới).
- Nhấn ✏️/🗑️ → mở chỉnh sửa hoặc xóa giai đoạn tương ứng.

## 3. Màn hình chỉnh sửa giai đoạn – MeStage Editor
### Mục đích
Cấu hình chi tiết một MeStage.

### Thành phần giao diện
- **Trường nhập**:
  - “Thời lượng (phút)” — số nguyên ≥1
  - “Lặp lại mỗi (phút)” — số nguyên ≥0 (0 = chỉ phát 1 lần)
- **Phần chọn âm thanh**:
  - Nhãn: “Âm thanh đã chọn: [N] file” (liệt kê ngắn gọn nếu cần)
  - Nút: “Chọn âm thanh…”
- **Nút “Lưu”**

### Hành vi
- Nhấn “Chọn âm thanh…” → mở Dialog chọn file.
- Sau khi chọn, cập nhật số lượng và tên file hiển thị.

## 4. Màn hình Thư viện âm thanh – Sound Library
### Mục đích
Quản lý tập trung các file âm thanh trong thư mục làm việc `/sdcard/DCIM/supportmeditation/`.

### Thành phần giao diện
- **Tiêu đề**: “Thư viện âm thanh”
- **Danh sách file dưới dạng list item hoặc card**:
  - Mỗi mục: tên file đã làm sạch + biểu tượng loa nhỏ.
- **Thanh công cụ trên cùng**:
  - Nút “+ Thêm” (biểu tượng +)
  - Nút “Chọn” → chuyển sang chế độ chọn nhiều
- **Chế độ chọn nhiều**:
  - Hiển thị checkbox trước mỗi file.
  - Nút “Xoá” xuất hiện khi có ít nhất 1 file được chọn.

### Hành vi
- Mở lần đầu → quét lại thư mục, không dùng cache.
- Nhấn “+ Thêm”:
  - Mở trình chọn file hệ thống (Storage Access Framework).
  - Người dùng chọn 1+ file.
  - Với mỗi file:
    - Hiển thị hộp thoại “Đặt tên file” với tên mặc định đã làm sạch.
    - Cho phép sửa tên.
    - Hệ thống làm sạch tên, xử lý trùng → copy vào thư mục làm việc.
    - Cập nhật danh sách ngay.
- Xoá file:
  - Chọn file → nhấn “Xoá” → xác nhận → xoá khỏi thư mục → cập nhật danh sách.

## 5. Màn hình Cài đặt – Settings
### Thành phần
- **Mức độ rung**: Radio group → Tắt / Nhẹ / Mạnh
- **Âm thanh mặc định**: Toggle → Bật / Tắt
- **Giảm sáng màn hình khi thiền**: Slider (0% – 100%, mặc định 30%)

### Lưu ý
- Cài đặt này là mặc định toàn cục, có thể ghi đè trong lúc thiền.

## 6. Dialog chọn âm thanh (cho MeStage)
### Mục đích
Chọn danh sách âm thanh cho một MeStage, tránh trùng lặp.

### Thành phần giao diện
- **Tiêu đề**: “Chọn âm thanh”
- **Danh sách cuộn các file từ thư mục làm việc**:
  - Mỗi mục: checkbox + tên file (đã làm sạch)
- **Nút “Xong”, “Hủy”**

### Hành vi
- Khi mở:
  - Tự động tích chọn các file đang có trong MeStage.sounds.
  - Các file đã chọn có thể bỏ chọn, nhưng không cho chọn lại nếu đã chọn (tránh trùng trong cùng giai đoạn).
- Nhấn “Xong” → cập nhật MeStage.sounds theo danh sách được chọn.

## 7. Màn hình Thiền – Meditation Timer
### Mục đích
Hiển thị trong lúc thiền — cung cấp thông tin và điều khiển cơ bản.

### Thành phần giao diện
- **Đồng hồ đếm ngược lớn** ở trung tâm (phút:giây)
- **Tên giai đoạn hiện tại** phía trên đồng hồ (ví dụ: “Giai đoạn: Thư giãn”)
- **Thanh điều khiển** (ẩn khi không chạm, hiện khi chạm):
  - ▶️/⏸️ Tạm dừng / Tiếp tục
  - 🛑 Dừng lại
  - 🔊/🔇 Bật/tắt âm thanh
  - 📳/📵 Bật/tắt rung

### Hành vi
- **Tạm dừng**:
  - Dừng đếm giờ.
  - Hiển thị xác nhận "Bạn có chắc muốn dừng?"
  - Dừng/tiếp tục phát âm thanh/rung.
  - Giữ nguyên trạng thái (có thể tiếp tục sau).
- **Tiếp tục**:
  - Tiếp tục đếm từ thời điểm dừng.
  - Tiếp tục phát âm thanh theo chu kỳ.
- **Dừng lại**:
  - Kết thúc buổi thiền.
  - Quay về màn hình ConfigList.
  - Không lưu tiến trình.
- **Tắt âm thanh/rung**:
  - Chỉ ảnh hưởng đến phiên thiền hiện tại.
  - Không thay đổi cài đặt hệ thống.
- **Gợi ý**: Ẩn thanh điều khiển sau 3–5 giây không chạm để tránh phân tâm.

## Lưu ý quan trọng
- **Chỉ sử dụng đơn vị phút**
- **Quản lý file âm thanh trực tiếp qua thư mục**, không dùng cơ sở dữ liệu
- **Giữ màn hình luôn bật** trong suốt phiên thiền, tự động tắt khi kết thúc/dừng

## Ghi chú triển khai
- Tất cả thao tác file (thêm/xoá/đổi tên) đều ảnh hưởng trực tiếp đến thư mục — không qua DB.
- Tên file luôn được làm sạch: chỉ giữ [a-zA-Z0-9_.-], thay ký tự khác bằng _.
- Không lưu cache danh sách file — quét lại mỗi lần mở Sound Library hoặc dialog chọn file.
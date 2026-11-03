# Stadium Ticket Booking System

Đồ án LAB211 - Group 3. Hệ thống bán vé sân vận động chạy trên console, viết bằng Java theo mô hình MVC. Dữ liệu được lưu trong các file CSV ở thư mục `data/`, không cần cài cơ sở dữ liệu.

## Tài khoản đăng nhập

Tất cả tài khoản mẫu đều dùng mật khẩu **`123456`**.

| Username | Password | Vai trò | Họ tên |
|---|---|---|---|
| `admin` | `123456` | Admin | System Admin |
| `seller1` | `123456` | Seller | Nguyen Van Seller |
| `seller2` | `123456` | Seller | Le Thi Ban Ve |
| `buyer1` | `123456` | Buyer | Tran Van Buyer |
| `khoa` | `123456` | Buyer | Dang Dang Khoa |

Có thể đăng ký thêm tài khoản Buyer bằng mục **2** ở menu chính.

## Cách chạy

Cần JDK 17 trở lên. Chạy các lệnh tại thư mục gốc của dự án, vì chương trình đọc dữ liệu theo đường dẫn tương đối `data/`.

**Windows (PowerShell):**

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

**Linux, macOS hoặc Git Bash:**

```bash
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -cp out Main
```

Khi demo chức năng hết hạn giữ ghế, có thể rút thời gian giữ ghế xuống 1 phút (mặc định 5 phút):

```bash
java -Dhold.minutes=1 -cp out Main
```

## Chức năng

**Admin**
- Quản lý người dùng và Seller
- Quản lý sân vận động, khu vực, ghế và đội bóng
- Xem tất cả trận đấu; giám sát booking, thanh toán, giao dịch
- Duyệt hoặc từ chối yêu cầu hủy vé (duyệt thì hủy vé và hoàn tiền)
- Xem báo cáo doanh thu và nhật ký hệ thống

**Seller**
- Thêm, sửa, xóa trận đấu; mở và đóng bán vé
- Tạo vé cho một ghế hoặc cả khu vực; sửa giá, tạm dừng bán, xóa vé
- Bán vé tại quầy, soát vé bằng mã QR
- Xem báo cáo doanh thu và lịch sử bán

**Buyer**
- Tìm kiếm trận đấu, xem thông tin trận và sơ đồ sân
- Chọn ghế, đặt vé, gia hạn giữ ghế, thanh toán (kết quả cổng thanh toán được mô phỏng)
- Xem lịch sử đặt vé và vé điện tử có mã QR
- Gửi yêu cầu hủy vé và theo dõi trạng thái

Cả ba vai trò đều có mục thông báo và hồ sơ cá nhân (cập nhật thông tin, đổi mật khẩu).

**Quy tắc đặt vé**
- Tối đa 4 vé trong một booking.
- Ghế được giữ 5 phút; hết hạn thì ghế tự động được trả lại.
- Được gia hạn giữ ghế 1 lần, thêm 5 phút.
- Không thể chọn ghế đang được giữ hoặc đã bán. Thao tác giữ ghế có kiểm tra phiên bản dữ liệu để tránh đặt trùng.

## Cấu trúc dự án

```text
src/
  Main.java      Điểm khởi chạy: tạo dữ liệu mẫu nếu thiếu, rồi mở MainController
  controller/    Điều khiển luồng và xử lý nghiệp vụ theo từng vai trò
  model/         Các thực thể (User, Match, Ticket, Booking, Payment...) và enums/
  view/          Nhập và xuất trên console
  repository/    Đọc và ghi file CSV
  service/       Tài khoản, thông báo, nhật ký, báo cáo, tạo dữ liệu mẫu
  dto/           Dữ liệu hiển thị (sơ đồ ghế, báo cáo doanh thu)
  exception/     Lỗi nghiệp vụ
  util/          Cấu hình và các hàm tiện ích
data/            Các file CSV dùng làm cơ sở dữ liệu
```

Luồng chạy: `Main` → `MainController` (đăng nhập, đăng ký) → `AdminController`, `SellerController` hoặc `BuyerController` → View.

## Dữ liệu

Mỗi file CSV trong `data/` tương ứng với một bảng, ví dụ `users.csv`, `matches.csv`, `tickets.csv`, `bookings.csv`, `payments.csv`. Dữ liệu được ghi trực tiếp vào các file này trong khi chạy chương trình.

Để khôi phục dữ liệu mẫu ban đầu, xóa thư mục `data/` rồi chạy lại chương trình. Các file sẽ được tạo lại.

## Thành viên

| Thành viên | Phần phụ trách |
|---|---|
| Hồ Lê Hoàng Nhật | Khởi chạy, Admin, sân, khu vực, ghế, đội bóng, nền tảng đọc ghi CSV, dữ liệu mẫu |
| Ngô Phạm Nguyệt Minh | Đăng nhập, đăng ký, hồ sơ, thông báo, nhật ký hệ thống |
| Bùi Khải Triều | Đặt vé, giữ ghế, thanh toán, giao dịch |
| Chu Bảo Khánh | Seller, quản lý trận đấu và vé, báo cáo doanh thu |
| Đặng Đăng Khoa | Buyer, sơ đồ ghế, vé điện tử, yêu cầu hủy vé |

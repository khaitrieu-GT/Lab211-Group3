# Bảng đối chiếu hợp nhất code vào nhánh `dev`

Tài liệu này cho biết **mỗi class / method trong 5 nhánh thành viên được đặt ở đâu** trong project hợp nhất `src/`.
Code gốc vẫn còn nguyên trên từng nhánh và trong lịch sử merge của `dev` (`git log --graph`).

**Quy ước khi hợp nhất**

- Các class trùng khái niệm (vd. 4 phiên bản `Seat`, 3 phiên bản `User`) được gộp thành **một class** chứa đủ method của mọi phiên bản.
- Mọi thực thể kế thừa `BaseEntity` (của Nhật), dùng **id kiểu `String`** và lưu bằng **CSV** qua `CsvRepository<T>`.
- Method "stub" chỉ in ra màn hình trong model (vd. `Match.addMatch()` in "Match added.") được chuyển thành nghiệp vụ thật trong **controller**, vì theo MVC thì model không in ra màn hình.
- Lỗi chính tả trong tên method được sửa: `crateHold → createHold`, `getsStatus → getStatus`, `EXPIIRED → EXPIRED`.
- **Ghế vật lý** (`Seat`, Admin quản lý) được tách khỏi **vé mở bán theo trận** (`Ticket`, nơi chặn double booking), để một ghế có thể bán cho nhiều trận khác nhau.

---

## 1. Hồ Lê Hoàng Nhật — `ho-le-hoang-nhat` (Admin, Model, Repository, CSV)

| Gốc | Vị trí mới |
|---|---|
| `Main.main` | `src/Main.java` |
| `controller.AdminController`: `getAllUsers`, `changeUserStatus`, `getAllStadiums`, `createStadium`, `getAllSeats`, `updateSeatMaintenance` | `controller/AdminController` (giữ nguyên tên; mở rộng thêm khu vực, ghế, đội bóng, giám sát, báo cáo) |
| `model.BaseEntity`: `getId`, `setId`, `toCsvLine` | `model/BaseEntity` |
| `model.User` (getter/setter, `toCsvLine`, `fromCsvLine`) | `model/User` (abstract) + `Admin`, `Seller`, `Buyer` |
| `model.Stadium`, `model.Section`, `model.Seat` (getter/setter, `toCsvLine`, `fromCsvLine`) | `model/Stadium`, `model/Section`, `model/Seat` |
| `model.SeatStatus` `AVAILABLE, LOCKED, BOOKED, MAINTENANCE` | `model/enums/SeatStatus` (`AVAILABLE, LOCKED, MAINTENANCE`) + trạng thái bán theo trận `model/enums/TicketStatus` (`HOLD` ≈ giữ chỗ, `SOLD` ≈ `BOOKED`) |
| `repository.*Repository`: `findAll`, `saveAll` | `repository/CsvRepository` (generic, có khoá theo file) |
| `StadiumRepository.add` | `CsvRepository.add` / `insert` |
| `UserRepository.findById`, `updateUserStatus` | `CsvRepository.findById`, `UserRepository.updateUserStatus` |
| `SeatRepository.updateStatus` (synchronized + version) | `SeatRepository.updateStatus` |
| `SectionRepository` (đang để trống) | `SectionRepository.findByStadiumId` |
| `view.AdminView`: `displayMenu`, `renderUserList`, `handleUserLock`, `renderStadiumList`, `handleAddStadium`, `handleSeatMaintenance` | `view/AdminView` (giữ nguyên tên) |
| `data/users.csv, stadiums.csv, sections.csv, seats.csv` | `data/` do `service/DataSeeder` sinh ra, vẫn giữ các bản ghi `U001–U003`, `STD01`, `SEC01–02`, `SEAT001–003` (SEAT003 = MAINTENANCE) |

## 2. Ngô Phạm Nguyệt Minh — `ngo-pham-nguyet-minh` (Đăng nhập, thông báo, audit log)

| Gốc | Vị trí mới |
|---|---|
| `controller.MainController.handleLogin` | `controller/MainController.handleLogin` (đọc `users.csv`, mật khẩu SHA-256) |
| `controller.MainController.triggerNotification` | `controller/MainController.triggerNotification` → `service/NotificationService` |
| `model.User`: `register`, `login`, `updateProfile`, `toCsvLine`, `fromCsvLine`, `getUserId`, `getUsername`, `getRole` | `model/User` (`getUserId` → `getId`; `passwordHash` → `password` đã băm) |
| `model.Notification`: `sendNotification`, `markAsRead`, `toCsvLine` | `model/Notification` (+ `fromCsvLine`, `NotificationRepository`) |
| `model.AuditLog`: `logAction`, `toCsvLine`, `fromCsvLine` | `model/AuditLog` + `service/AuditService` (ghi log cho mọi thao tác quan trọng) |
| `view.Main.main` | `view/MainView.start` (đăng nhập → điều hướng theo vai trò) |
| Tài liệu `MVC`, sơ đồ `.drawio.png` | `docs/ngo-pham-nguyet-minh/` |

## 3. Chu Bảo Khánh — `chu-bao-khanh` (Seller)

| Gốc | Vị trí mới |
|---|---|
| `SellerController`: `createMatch`, `updateMatch`, `deleteMatch`, `getMatches`, `openSale`, `closeSale`, `createTicket`, `updateTicket`, `getTickets`, `sellTicket` | `controller/SellerController` (giữ nguyên tên; lưu CSV, Seller chỉ thao tác trên trận của mình) |
| `model.Match`: `openSale`, `closeSale`, getter/setter, `toString` | `model/Match` |
| `model.Match`: `addMatch`, `updateMatch`, `deleteMatch` (stub) | `SellerController.createMatch / updateMatch / deleteMatch` |
| `model.Match.viewMatch` | `view/MatchView.displayMatch` |
| `model.Ticket`: `putOnSale`, `stopSale`, getter/setter, `toString` | `model/Ticket` (vé mở bán cho 1 ghế trong 1 trận) |
| `model.Ticket`: `createTicket`, `updateTicket` (stub) | `SellerController.createTicket / updateTicket` (+ `generateTicketsForSection`) |
| `model.Ticket.sellTicket` | `SellerController.sellTicket` → `BookingController.sellAtCounter` (giữ ghế → thanh toán CASH → phát e-ticket) |
| `model.Ticket.viewTicket` | `Ticket.toString` / `TicketView.showTickets` |
| `model.Seat`: `updateStatus`, `getVersion`, getter/setter | `model/Seat.updateStatus` |
| `model.Section`: `updateSection`, `updateStatus`, `getSeats/setSeats` | `model/Section` (+ `AdminController.updateSection / updateSectionStatus`) |
| `model.Seller` (+ các stub `createMatch…sellTicket`) | `model/Seller extends User`; nghiệp vụ nằm trong `SellerController` |
| `model.BookingTransaction`: `addTicket` (tối đa 4 vé), `getTotalAmount`, `getTickets`, `getOrderStatus` | `model/Booking.addItem` (`AppConfig.MAX_TICKETS_PER_BOOKING = 4`), `getTotalAmount`, `getItems`, `getStatus` |
| `view.SellerView`: `showMenu`, `showMatches`, `showTickets`, `showMessage` | `view/SellerView` (`showMessage` nằm trong `BaseView`) |

## 4. Đặng Đăng Khoa — `dang-dang-khoa` (Buyer)

| Gốc | Vị trí mới |
|---|---|
| `Main.main`, `createBuyer`, `createMatch` | `Main` + `DataSeeder` (user `khoa`, trận `M001 MU vs Liverpool`, đội `T001–T002`, sân `STD02 Old Trafford`, khu `SEC03–04`) |
| `MatchController`: `searchByName`, `searchByDate`, `findById`, `viewMatch` | `controller/MatchController` (giữ nguyên tên) |
| `SeatController`: `viewSeatMap`, `selectSeat`, `unselectSeat` | `controller/SeatController` (giữ nguyên tên) |
| `OrderController.createOrder` | `BookingController.holdSeat` (gom ghế vào booking PENDING) |
| `OrderController.confirmOrder` | `BookingController.payBooking` |
| `OrderController.viewPurchaseHistory` | `BookingController.viewPurchaseHistory` |
| `TicketController`: `createTicket`, `viewTicket`, `getLatestTicket` | `controller/TicketController` (giữ nguyên tên) |
| `CancellationController`: `createRequest`, `viewStatus` | `controller/CancellationController` (+ `approveRequest`, `rejectRequest` cho Admin) |
| `model.Order`: `addItem`, `calculateTotal`, `confirmOrder`, `cancelOrder` | `model/Booking.addItem / calculateTotal / confirm / cancel` |
| `model.OrderItem.calculateSubtotal` | `model/BookingSeat.calculateSubtotal` |
| `model.Seat`: `select`, `unselect`, `sell`, `isAvailable` | `model/Ticket.lock / unlock / markAsSold / isAvailable` (trạng thái theo trận), `Seat.isAvailable` |
| `model.SeatSection`: `addSeat`, `getAvailableSeats` | `model/Section.addSeat / getAvailableSeats` |
| `model.Stadium.addSection` | `model/Stadium.addSection / getSections` |
| `model.Team` | `model/Team` |
| `model.Ticket`: `validateTicket`, `cancelTicket` | `model/ETicket.validateTicket / cancel` (+ `checkIn`) |
| `model.CancellationRequest`: `submitRequest`, `approveRequest`, `rejectRequest` | `model/CancellationRequest` |
| `model.User`: `addOrder`, `getOrders` | `model/Buyer`; lịch sử lấy từ `BookingRepository` qua `BookingController.viewPurchaseHistory` |
| `view.MainView`: `showMenu`, `readInt`, `readString` | `view/BuyerView.showMenu`, `view/ConsoleInput.readInt / readString` |
| `MatchView`, `SeatView`, `OrderView`, `TicketView` (mọi method `display*`) | cùng tên trong `view/` |
| `build.xml`, `nbproject/` | `build.xml`, `nbproject/` ở thư mục gốc |

## 5. Bùi Khải Triệu — `bui-khai-trieu` (Booking core, Command, chống double booking)

| Gốc | Vị trí mới |
|---|---|
| `Main.main` (14 bước: ghế → zone → sân → user → booking → giữ ghế → thanh toán → giao dịch → xác nhận → vé → test double booking) | `controller/BookingController` (`holdSeat`, `payBooking`, `releaseExpiredHolds`, `simulateDoubleBooking`) + `view/BuyerView` |
| `command.Command` + 21 command | `command/` giữ **đủ 22 file, đúng tên**. `RefundPaymentCommand` thêm đuôi `.java` còn thiếu; `UpdateProfileCommand` sửa lỗi truyền sai tham số |
| `LockSeatCommand`, `UnlockSeatCommand`, `MarkSeatAsSoldCommand` | đối tượng nhận lệnh là `Ticket` (ghế của trận) |
| `CancelTicketCommand`, `GenerateTicketQrCommand` | đối tượng nhận lệnh là `ETicket` |
| — | thêm `command/CommandInvoker` |
| `enums.*` (9 enum) | `model/enums/` — `TicketStatus` → `ETicketStatus`; `PaymentMethod` thêm `CASH`; `SeatHoldStatus` thêm `CONFIRMED` |
| `model.Booking`: `create`, `confirm`, `cancel`, `getTotalAmount` | `model/Booking` (+ `expire`) |
| `model.BookingSeat`: `lockSeat`, `releaseSeat`, `markAsSold` | `model/BookingSeat` (+ `cancelSold`) |
| `model.Payment`: `createPayment`, `verifyPayment`, `updateStatus`, `refund` | `model/Payment` (+ `refund(amount)` hoàn một phần) |
| `model.Transaction`: `process`, `verify`, `getsStatus` | `model/Transaction` (`getStatus`) + `service/PaymentGateway` mô phỏng cổng thanh toán |
| `model.SeatHold`: `crateHold`, `extendHold`, `releaseHold`, `isExpired` | `model/SeatHold` (`createHold`, + `confirmHold`, giới hạn số lần gia hạn) |
| `model.Seat`: `lock`, `unlock`, `markAsSold`, `isAvailable` | `model/Ticket` (bán theo trận); `Seat.lock / unlock` dùng cho Admin khoá ghế |
| `model.Stadium`: `getZones`, `getAllSeats` | `model/Stadium.getSections / getAllSeats` |
| `model.Zone`: `getSeats`, `getAvailableSeats` | `model/Section` |
| `model.Ticket`: `generateQr`, `viewTicket`, `cancel` | `model/ETicket` |
| `model.User`: `getProfile`, `updateProfile`, `changePassword` | `model/User` |

---

## Các tên đã đổi khi gộp (kết quả kiểm tra tự động)

Mọi class và method khác của 5 nhánh vẫn giữ **đúng tên** trong `src/`. Riêng các tên dưới đây là bản trùng nghĩa, đã được gộp vào một tên chung:

| Tên gốc | Nhánh | Tên trong `dev` |
|---|---|---|
| class `Order`, `BookingTransaction` | Khoa, Khánh | `model/Booking` |
| class `OrderItem` | Khoa | `model/BookingSeat` |
| class `SeatSection`, `Zone` | Khoa, Triệu | `model/Section` |
| class `OrderController` | Khoa | `controller/BookingController` |
| `Match.addMatch()` | Khánh | `SellerController.createMatch` |
| `Main.createBuyer()` | Khoa | `DataSeeder.seedUsers` |
| `User.addOrder()`, `User.getOrders()` | Khoa | `BookingController.holdSeat` / `viewPurchaseHistory` |
| `Order.getBuyer()`, `BookingTransaction.getFanId()/setFanId()` | Khoa, Khánh | `Booking.getUserId()` |
| `Order.getOrderDate()`, `BookingTransaction.getTransactionDate()/setTransactionDate()` | Khoa, Khánh | `Booking.getCreatedAt()` |
| `BookingTransaction.getOrderStatus()` | Khánh | `Booking.getStatus()` |
| `BookingTransaction.setTotalAmount()` | Khánh | `Booking.calculateTotal()` (tự tính từ các ghế) |
| `OrderItem.getQuantity()` | Khoa | mỗi `BookingSeat` là 1 ghế (số lượng luôn = 1) |
| `Ticket.getOrder()` (vé của Khoa) | Khoa | `ETicket.getBookingId()` |
| `Seat.getRow()/setRow()`, `Seat.getNumber()` | Khoa, Khánh | `Seat.getRowNumber()/setRowNumber()`, `Seat.getSeatNumber()` |
| `Match.getTitle()` | Khoa | `Match.getCompetition()` |
| `Ticket.setTicketId()` | Khánh | `BaseEntity.setId()` |
| `User.setRole()` | Nhật | vai trò cố định theo lớp con, tạo bằng `User.create(Role, …)` |

---

## Thành phần mới thêm để nối thành luồng hoàn chỉnh

| Thành phần | Vai trò |
|---|---|
| `controller/PaymentController` | Tạo Payment → Transaction → verify → SUCCESS/FAILED; hoàn tiền |
| `controller/ProfileController`, `view/ProfileView` | Hồ sơ, đổi mật khẩu, thông báo cho mọi vai trò |
| `service/ReportService`, `dto/SalesReport` | Báo cáo doanh thu (Admin: toàn hệ thống, Seller: trận của mình) |
| `dto/SeatMap` | Sơ đồ sân theo trận (sân → khu → ghế + trạng thái vé) |
| `service/DataSeeder` | Sinh dữ liệu mẫu khi thiếu file CSV |
| `exception/BusinessException`, `view/BaseView` | Controller ném lỗi nghiệp vụ, View bắt và hiển thị |

# Kịch bản demo — Stadium Ticket Booking System (LAB211 · Group 3)

Kịch bản gồm **8 màn**, khoảng **20–25 phút**, đi qua **toàn bộ chức năng** của 3 vai trò Admin, Seller, Buyer, cùng các tình huống lỗi quan trọng: sai mật khẩu, ghế bảo trì, thanh toán thất bại, double booking, hết hạn giữ ghế, tài khoản bị khoá, soát vé hai lần.

> Làm **đúng thứ tự** và nhập **đúng giá trị** trong bảng thì mọi mã (STD03, M004, TK0025, BK0002, ET0001, CR0001…) sẽ trùng với tài liệu. Kịch bản đã được chạy kiểm chứng bằng `demo/run-demo.ps1`.

---

## 0. Chuẩn bị (trước giờ demo)

1. Khôi phục dữ liệu mẫu: **xoá thư mục `data/`** (chương trình tự sinh lại khi chạy).
2. Mở terminal tại thư mục gốc dự án, rút thời gian giữ ghế xuống **2 phút** để kịp demo phần hết hạn:
   ```bat
   set JAVA_OPTS=-Dhold.minutes=2
   run.bat
   ```
   (Linux/macOS: `JAVA_OPTS=-Dhold.minutes=2 ./run.sh`)
3. Tập dượt tự động (không đụng tới `data/` thật, log nằm ở `demo/output/logs/`):
   ```bat
   powershell -ExecutionPolicy Bypass -File demo\run-demo.ps1
   ```

**Tài khoản mẫu** (mật khẩu `123456`): `admin`, `seller1`, `seller2`, `buyer1`, `khoa`.

**Phân công trình bày gợi ý** (mỗi người demo phần mình đã code):

| Màn | Nội dung | Người trình bày |
|---|---|---|
| 1 | Admin dựng sân, khu vực, ghế, đội, tài khoản Seller | Hồ Lê Hoàng Nhật |
| 2 | Seller tạo trận, tạo vé, mở bán | Chu Bảo Khánh |
| 3 | Buyer đăng ký, tìm trận, chọn ghế, thanh toán, e-ticket, yêu cầu huỷ | Đặng Đăng Khoa |
| 4, 7 (double booking), 8 | Giữ ghế có hạn, chống double booking, hết hạn giữ ghế | Bùi Khải Triệu |
| 5, 6, 7 | Bán quầy, soát vé, thông báo, khoá tài khoản, duyệt huỷ, audit log | Ngô Phạm Nguyệt Minh |

Cách đọc bảng: `2 › 4` nghĩa là chọn menu **2** rồi chọn tiếp **4**; `⏎` là bấm Enter để bỏ trống.

---

## Màn 1 — Admin dựng hạ tầng (≈3 phút)

| # | Thao tác | Nhập | Kết quả mong đợi | Điểm nhấn |
|---|---|---|---|---|
| 1.1 | Đăng nhập sai | `1` · `admin` · `sai-mat-khau` | `!! Sai ten dang nhap hoac mat khau!` | Mật khẩu lưu dạng SHA-256, sai mật khẩu có ghi audit log |
| 1.2 | Đăng nhập Admin | `1` · `admin` · `123456` | Menu ADMIN, có `(1 thong bao moi)` | Điều hướng theo vai trò (đa hình User → Admin) |
| 1.3 | Thêm sân | `2 › 2` · `San Van Dong Thong Nhat` · `138 Dao Duy Tu, Quan 10` · `TP Ho Chi Minh` · `25000` | `Them San van dong thanh cong: STD03` | Địa chỉ có dấu phẩy vẫn lưu đúng (CSV có quoting) |
| 1.4 | Thêm khu vực | `4` · `STD03` · `Khan dai A` · `VIP` · `100` · `400000` | `Them khu vuc thanh cong: SEC05` | Tổng sức chứa các khu không được vượt sức chứa sân |
| 1.5 | Sinh hàng ghế | `7` · `STD03` · `SEC05` · `A` · `5` | `Da tao 5 ghe.` (SEAT026–SEAT030) | |
| 1.6 | Sửa khu vực | `5` · `STD03` · `SEC05` · `Khan dai A (VIP)` · `120` | `Cap nhat khu vuc thanh cong!` | |
| 1.7 | Xem sơ đồ sân | `3` · `STD03` · rồi `0` | Khu SEC05 với 5 ghế A1–A5 | |
| 1.8 | Thêm đội bóng | `3 › 2` · `Lao` · `lao.png` · `Laos`, rồi `1` xem danh sách, `0` | `Them doi thanh cong: T006` | |
| 1.9 | Tạo tài khoản Seller | `1 › 3` · `seller3` · `123456` · `Pham Van Ban` · `seller3@stadium.com` · `0900000006`, rồi `1` xem danh sách, `0` | `Tao Seller thanh cong: U006` | Chỉ Admin cấp được tài khoản Seller |
| 1.10 | Đăng xuất | `0` | | |

## Màn 2 — Seller tạo trận và mở bán (≈3 phút)

| # | Thao tác | Nhập | Kết quả mong đợi | Điểm nhấn |
|---|---|---|---|---|
| 2.1 | Đăng nhập | `1` · `seller3` · `123456` | Menu SELLER MANAGEMENT | |
| 2.2 | Thêm trận | `1` · `Viet Nam vs Lao` · `AFF Cup` · `T003` · `T006` · `STD03` · `2027-06-01` · `19:00` | `Tao tran thanh cong: M004` | Kiểm tra đội khác nhau, sân tồn tại, ngày ở tương lai |
| 2.3 | Mở bán khi chưa có vé | `5` · `M004` | `!! Tran chua co ve AVAILABLE nao…` | Ràng buộc nghiệp vụ |
| 2.4 | Tạo vé lẻ | `7` · `M004` · `SEC05` · `SEAT026` · `⏎` · `450000` | `Tao ve thanh cong: TK0025 - 450,000 VND` | |
| 2.5 | Tạo vé cả khu | `8` · `M004` · `SEC05` · `⏎` | `Da tao 4 ve moi cho khu SEC05.` (TK0026–0029, giá gốc 400,000) | Bỏ qua ghế đã có vé |
| 2.6 | Xem vé | `9` · `M004` | 5 vé AVAILABLE | |
| 2.7 | Sửa giá vé | `10` · `M004` · `TK0029` · `⏎` · `380000` | `Da cap nhat: TK0029 … 380,000 VND` | |
| 2.8 | Dừng bán rồi mở lại 1 vé | `11` · `M004` · `TK0028` · `1`, rồi `11` · `M004` · `TK0028` · `2` | `TK0028 -> CANCELLED`, rồi `-> AVAILABLE` | |
| 2.9 | Đổi giờ trận | `3` · `M004` · `⏎` · `⏎` · `⏎` · `20:00` | `Cap nhat tran thanh cong!` | |
| 2.10 | Mở bán | `5` · `M004` | `Tran M004 da MO BAN ve.` | |
| 2.11 | Tạo rồi xoá trận nháp | `1` · `Tran thu nghiem` · `Giao huu` · `T001` · `T006` · `STD03` · `2027-07-01` · `18:00`, rồi `4` · `M005` · `Y` | `M005` được tạo, rồi `Da xoa tran M005` | Chỉ xoá được khi chưa có vé giữ/đã bán |
| 2.12 | Xoá trận của Seller khác | `4` · `M003` · `Y` | `!! Khong tim thay tran M003 trong danh sach tran cua ban.` | Seller không sửa được dữ liệu của Seller khác |
| 2.13 | Xem trận của tôi, đăng xuất | `2`, `0` | Chỉ thấy M004 (trận của seller3) | |

## Màn 3 — Buyer mua vé từ đầu đến cuối (≈6 phút)

| # | Thao tác | Nhập | Kết quả mong đợi | Điểm nhấn |
|---|---|---|---|---|
| 3.1 | Đăng ký | `2` · `fan01` · `123456` · `Nguyen Van Fan` · `fan01@gmail.com` · `0912345678` | `Dang ky thanh cong! Ma tai khoan: U007` | Kiểm tra username, email, mật khẩu ≥ 6 ký tự |
| 3.2 | Đăng nhập | `1` · `fan01` · `123456` | Menu BUYER `(1 thong bao moi)` | |
| 3.3 | Hồ sơ: xem, sửa, đổi mật khẩu | `11` › `1`; `2` · `⏎` · `⏎` · `0987654321` · `fan.png`; `3` · `123456` · `fan2027` · `fan2027`; `0` | `Cap nhat ho so thanh cong!`, `Doi mat khau thanh cong!` | UpdateProfileCommand, ChangePasswordCommand |
| 3.4 | Tìm trận theo tên / ngày / tất cả | `1` · `1` · `lao`; `1` · `2` · `2027-06-01`; `1` · `3` | Lần lượt: chỉ M004; chỉ M004; M001–M004 | |
| 3.5 | Xem thông tin trận | `2` · `M004` | Đội, sân Thống Nhất, giờ 20:00, `OPEN (dang mo ban ve)` | |
| 3.6 | Xem sơ đồ sân | `3` · `M004` | `Hang A: [A1  ] … [A5  ]`, còn trống 5 ghế | |
| 3.7 | Chọn ghế bảo trì + giới hạn 4 vé | `4` · `M002` · `A3` · `A1` · `A2` · `B1` · `B2` | `!! Ghe A3 dang MAINTENANCE.`; giữ được 4 ghế; `Da dat so ve toi da cho booking nay.` | Tối đa 4 vé/booking |
| 3.8 | Huỷ booking | `4` · `Y` | `Da huy booking va tra ghe.` (BK0001) | Ghế trả về AVAILABLE ngay |
| 3.9 | Chọn 3 ghế trận M004 | `4` · `M004` · `A1` · `A2` · `A3` · `⏎` | Màn hình BOOKING BK0002, tổng 1,250,000, đếm ngược thời gian giữ | AVAILABLE → **HOLD** (LockSeatCommand + version) |
| 3.10 | Thanh toán **thất bại** | `1` · `2` (MOMO) · `2` (thất bại) | `Trang thai: FAILED`, `!! Thanh toan that bai. Ghe van dang duoc giu…` | Thất bại không làm mất ghế |
| 3.11 | Gia hạn giữ ghế | `2` | `Da gia han giu ghe.` (thêm 5 phút, chỉ được 1 lần) | ExtendHoldCommand |
| 3.12 | Bỏ chọn ghế A3 | `3` · `BS0007` | `Da bo chon ghe.`, tổng còn 850,000 | |
| 3.13 | Thanh toán **thành công** | `1` · `1` (VNPAY) · `1` (thành công) | `SUCCESS`, in 2 E-TICKET ET0001 (A1), ET0002 (A2) kèm mã QR | HOLD → **SOLD**, ConfirmBooking, GenerateTicketQr |
| 3.14 | Lịch sử đặt vé | `6` | BK0002 CONFIRMED (A1, A2 SOLD; A3 CANCELLED); BK0001 CANCELLED | |
| 3.15 | Xem e-ticket | `7` · `ET0001` | Vé chi tiết. **Chép lại mã QR của ET0001** để dùng ở màn 5 | |
| 3.16 | Gửi yêu cầu huỷ vé | `8` · `ET0002` · `Ban viec dot xuat` | `Da gui yeu cau…` (CR0001) | Admin nhận được thông báo |
| 3.17 | Trạng thái yêu cầu, thông báo, đăng xuất | `9`, `10`, `0` | CR0001 PENDING; danh sách thông báo `[*]` | |

## Màn 4 — Giữ ghế rồi bỏ đi (≈1 phút)

| # | Thao tác | Nhập | Kết quả mong đợi | Điểm nhấn |
|---|---|---|---|---|
| 4.1 | `khoa` giữ ghế V1 trận M001 | `1` · `khoa` · `123456` · `4` · `M001` · `V1` · `⏎` | `Da giu ghe V1 (khu SEC04) - 1,000,000 VND` (BK0003) | |
| 4.2 | Không thanh toán, xem booking chờ | `0` · `5` · `BK0003` · `0` · `0` (đăng xuất) | BK0003 PENDING, còn khoảng 2 phút | Phần hết hạn sẽ được kiểm tra ở màn 8 |

## Màn 5 — Seller bán tại quầy và soát vé (≈2 phút)

| # | Thao tác | Nhập | Kết quả mong đợi | Điểm nhấn |
|---|---|---|---|---|
| 5.1 | Đăng nhập | `1` · `seller3` · `123456` | | |
| 5.2 | Bán tại quầy cho buyer1 | `12` · `M004` · `TK0028` · `buyer1` | `Ban ve thanh cong (tien mat).`, ET0003 | Cùng luồng giữ ghế → thanh toán (CASH) → e-ticket |
| 5.3 | Soát vé QR | `13` · *(mã QR ET0001 đã chép ở 3.15)* | `Check-in thanh cong: Viet Nam vs Lao - Khu SEC05 - Ghe A1` | ACTIVE → USED |
| 5.4 | Soát lại cùng vé | `13` · *(cùng mã QR)* | `!! Ve khong hop le (trang thai USED).` | Chống dùng lại vé |
| 5.5 | Báo cáo, lịch sử bán, thông báo, hồ sơ | `14`, `15`, `16`, `17` › `1` › `0` | Báo cáo M004: 5 vé, **đã bán 3**, doanh thu **1,250,000 VND** | |
| 5.6 | Đăng xuất | `0` | | |

## Màn 6 — Phân quyền Seller và yêu cầu huỷ thứ hai (≈1 phút)

| # | Thao tác | Nhập | Kết quả mong đợi | Điểm nhấn |
|---|---|---|---|---|
| 6.1 | seller1 soát vé của trận M004 | `1` · `seller1` · `123456` · `13` · *(mã QR của ET0003, xem ở 5.2)* · `0` | `!! Ve nay khong thuoc tran dau cua ban.` | Seller chỉ soát vé trận của mình |
| 6.2 | buyer1 xem vé, gửi yêu cầu huỷ | `1` · `buyer1` · `123456` · `7` · `⏎` · `8` · `ET0003` · `Mua nham tran` · `0` | ET0003 trong danh sách; CR0002 được tạo | |

## Màn 7 — Admin vận hành (≈5 phút)

| # | Thao tác | Nhập | Kết quả mong đợi | Điểm nhấn |
|---|---|---|---|---|
| 7.1 | Đăng nhập | `1` · `admin` · `123456` | `(… thong bao moi)` vì có yêu cầu huỷ | |
| 7.2 | Xem yêu cầu chờ duyệt | `6 › 1` | CR0001, CR0002 PENDING | |
| 7.3 | **Duyệt** CR0001 | `2` · `CR0001` | `Thanh toan PM0002 da hoan 400,000 VND (SUCCESS)` | Hoàn một phần; ET0002 bị huỷ, TK0026 quay lại bán |
| 7.4 | **Từ chối** CR0002 | `3` · `CR0002` · `Ve ban tai quay khong ho tro hoan` | `Da tu choi yeu cau CR0002.` | |
| 7.5 | Xem tất cả yêu cầu | `4` · `0` | CR0001 APPROVED, CR0002 REJECTED | |
| 7.6 | Giám sát | `5` › `1` (booking), `2` (thanh toán), `3` (giao dịch), `0` | PM0001 FAILED, PM0002 SUCCESS (hoàn 400,000), PM0003 CASH… | |
| 7.7 | Báo cáo doanh thu | `7` | M004: đã bán 2, **850,000 VND**; đã hoàn 400,000; M001 có 1 vé **Dang giu** (của khoa) | |
| 7.8 | **Kiểm thử double booking** | `9` · `TK0027` | `[THANH CONG]` một người, `[BI CHAN] … Ghe vua duoc nguoi khac giu truoc`; trạng thái cuối AVAILABLE | 2 luồng cùng đọc version, chỉ 1 luồng ghi được (optimistic locking) |
| 7.9 | Ghế bảo trì | `2 › 8` · `SEAT010` · `Y` | `… Da dung ban 1 ve cua ghe nay.` | Ghế bảo trì thì vé của ghế đó tự dừng bán |
| 7.10 | Khoá / mở khoá ghế | `9` · `SEAT011` · `1`; `9` · `SEAT011` · `2` | `Da khoa ghe. Da dung ban 1 ve…`, `Da mo khoa ghe.` | |
| 7.11 | Đóng / mở khu vực | `6` · `STD01` · `SEC02` · `2`; `6` · `STD01` · `SEC02` · `1`; `1`; `0` | `SEC02 -> CLOSED`, `-> ACTIVE`; danh sách 3 sân | |
| 7.12 | Khoá tài khoản fan01 | `1 › 2` · `U007` · `2` · `0` | `Cap nhat trang thai User thanh cong!` | fan01 nhận thông báo |
| 7.13 | Danh sách trận, audit log, thông báo | `4`, `8`, `10`, `0` (đăng xuất) | Audit log ghi mọi thao tác (LOGIN, HOLD, PAYMENT, APPROVE_CANCEL…) | |
| 7.14 | fan01 đăng nhập khi bị khoá | `1` · `fan01` · `fan2027` | `!! Tai khoan dang o trang thai LOCKED…` | |
| 7.15 | Admin mở khoá | `1` · `admin` · `123456` · `1 › 2` · `U007` · `1` · `0` · `0` | Cập nhật thành công | |
| 7.16 | fan01 xem kết quả huỷ | `1` · `fan01` · `fan2027` · `9`, `7` · `⏎`, `10`, `0` | CR0001 APPROVED; ET0001 USED, ET0002 CANCELLED; thông báo hoàn tiền | |

## Màn 8 — Hết hạn giữ ghế và đóng bán (≈1 phút)

> Đến đây đã qua hơn 2 phút kể từ màn 4. Nếu chưa đủ thì chờ thêm một chút.

| # | Thao tác | Nhập | Kết quả mong đợi | Điểm nhấn |
|---|---|---|---|---|
| 8.1 | khoa xem booking chờ | `1` · `khoa` · `123456` · `5` | `Khong co booking nao dang cho thanh toan.` | Hệ thống tự trả ghế khi hết hạn |
| 8.2 | Lịch sử, thông báo | `6`, `10`, `0` | BK0003 **EXPIRED**; `Booking BK0003 da het thoi gian giu ghe, cac ghe da duoc tra lai.` | V1 quay lại AVAILABLE |
| 8.3 | Seller đóng bán | `1` · `seller3` · `123456` · `6` · `M004` · `2` · `0` · `0` | `Tran M004 da DONG BAN ve.`, trạng thái CLOSED; thoát chương trình | |

---

## Bảng phủ chức năng

| Vai trò | Chức năng (menu) | Bước |
|---|---|---|
| Chung | Đăng nhập đúng / sai / tài khoản bị khoá · Đăng ký · Đăng xuất · Thoát | 1.1, 1.2, 7.14, 3.1, 1.10, 8.3 |
| Chung | Hồ sơ: xem / sửa / đổi mật khẩu · Thông báo | 3.3, 5.5 · 3.17, 7.13 |
| Admin | Người dùng: danh sách, khoá/mở khoá, tạo Seller | 1.9, 7.12, 7.15 |
| Admin | Sân: danh sách, thêm, sơ đồ · Khu: thêm, sửa, đóng/mở · Ghế: sinh hàng, bảo trì, khoá/mở khoá | 1.3–1.7, 7.9–7.11 |
| Admin | Đội bóng · Tất cả trận · Giám sát booking/thanh toán/giao dịch | 1.8, 7.13, 7.6 |
| Admin | Yêu cầu huỷ: chờ duyệt / duyệt / từ chối / tất cả · Báo cáo · Audit log · Double booking | 7.2–7.5, 7.7, 7.13, 7.8 |
| Seller | Thêm / xem / sửa / xoá trận (của mình và của người khác) · Mở / đóng bán | 2.2, 2.13, 2.9, 2.11–2.12, 2.3, 2.10, 8.3 |
| Seller | Tạo vé lẻ / cả khu · Xem · Sửa giá · Dừng/mở lại vé · Bán quầy | 2.4–2.8, 5.2 |
| Seller | Soát vé (thành công / vé đã dùng / vé trận khác) · Báo cáo · Lịch sử bán | 5.3, 5.4, 6.1, 5.5 |
| Buyer | Tìm trận (tên/ngày/tất cả) · Xem trận · Sơ đồ sân | 3.4–3.6 |
| Buyer | Chọn ghế (bảo trì, tối đa 4) · Huỷ booking · Thanh toán thất bại/thành công · Gia hạn · Bỏ chọn | 3.7–3.13 |
| Buyer | Booking chờ thanh toán · Lịch sử · E-ticket · Yêu cầu huỷ · Trạng thái yêu cầu | 4.2, 3.14, 3.15, 3.16, 3.17 |
| Hệ thống | Giữ ghế có hạn, tự trả ghế khi hết hạn · Chống double booking · Hoàn tiền một phần · Audit log | 4.1, 8.1–8.2, 7.8, 7.3, 7.13 |

## Nếu demo bị lệch

- Lệch một bước thì các mã phía sau (BK, BS, ET, CR) có thể khác. Hãy đọc mã thật trên màn hình rồi nhập theo mã đó.
- Muốn làm lại từ đầu: thoát chương trình, xoá `data/`, chạy lại `run.bat`.
- Thời gian giữ ghế đã hết trước khi kịp thanh toán ở màn 3: chọn lại ghế, hoặc chạy với `-Dhold.minutes=5`.

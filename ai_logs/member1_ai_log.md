## Ngày: 26/09/2026
- **Công việc:** Khởi tạo 4 file dữ liệu cơ sở (.csv) trong thư mục data/.
- **Nội dung:** Đã hoàn thành tạo users.csv, stadiums.csv, sections.csv và seats.csv có chứa trường version phục vụ kiểm soát xung đột.
## Ngày: 27/09/2026
- **Task:** Viết toàn bộ các Class Entity thuộc tầng Model (BaseEntity, User, Stadium, Section, Seat, SeatStatus).
- **Prompt:** "Hướng dẫn triển khai các file Java tầng Model bám sát Class Diagram."
- **Kết quả:** Đã tạo xong 6 file Java trong gói model/ với đầy đủ getter/setter, phương thức toCsvLine(), fromCsvLine() và thuộc tính version cho Seat.
## Ngày: 27/09/2026
- **Task:** Tối ưu hóa mã nguồn toàn bộ các Class theo chuẩn Encapsulation (Private fields & this keywords).
- **Prompt:** "Tôi muốn sử dụng thuộc tính private và từ khóa this cho tất cả các class trong dự án."
- **Kết quả:** Đã cập nhật 100% thuộc tính trong gói model, repository, controller và view sang phạm vi private, đồng thời bổ sung từ khóa this tường minh tại các constructor và phương thức nội bộ.
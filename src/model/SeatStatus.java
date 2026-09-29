package model;

public enum SeatStatus {    
    AVAILABLE,  // Ghế trống có thể đặt (TV3 & TV4 sử dụng)
   LOCKED,  // Ghế đang trong khoảng thời gian giữ / Hold (TV4 sử dụng)
   BOOKED,  // Ghế đã mua thành công (TV2 & TV4 sử dụng)
   MAINTENANCE  // Ghế bị khóa bảo trì (Do Admin - Bạn quản lý)
}




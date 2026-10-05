package model.enums;

/**
 * Trang thai vat ly cua ghe trong san (do Admin quan ly).
 * Trang thai ban ve theo tung tran nam o {@link TicketStatus}.
 */
public enum SeatStatus {
    AVAILABLE,   // Ghe su dung binh thuong
    LOCKED,      // Admin khoa ghe
    MAINTENANCE  // Ghe dang bao tri
}

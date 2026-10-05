package model.enums;

/**
 * Trang thai ve mo ban cua mot ghe trong mot tran.
 * AVAILABLE -> HOLD -> SOLD, HOLD het han -> AVAILABLE.
 */
public enum TicketStatus {
    AVAILABLE,
    HOLD,
    SOLD,
    CANCELLED   // Seller dung ban ve nay
}

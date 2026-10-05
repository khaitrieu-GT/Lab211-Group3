package model.enums;

/** Trang thai tran dau / dot mo ban (Khanh: OPEN/CLOSED, Khoa: UPCOMING). */
public enum MatchStatus {
    UPCOMING,   // Da tao, chua mo ban
    OPEN,       // Dang mo ban ve
    CLOSED,     // Da dung ban ve
    CANCELLED
}

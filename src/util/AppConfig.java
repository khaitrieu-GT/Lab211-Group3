package util;

/**
 * Cau hinh chung cua he thong.
 */
public final class AppConfig {

    /** Thu muc chua file CSV (chay chuong trinh tu thu muc goc du an). */
    public static final String DATA_DIR = "data";

    /** Thoi gian giu ghe (phut) truoc khi tu dong tra ghe. */
    public static final int HOLD_MINUTES = 5;

    /** So phut moi lan gia han giu ghe (Trieu: SeatHold.extendHold). */
    public static final int HOLD_EXTEND_MINUTES = 5;

    /** So lan toi da duoc gia han giu ghe. */
    public static final int MAX_HOLD_EXTENSIONS = 1;

    /** So ve toi da trong mot booking (Khanh: BookingTransaction.addTicket). */
    public static final int MAX_TICKETS_PER_BOOKING = 4;

    /** Ma phan hoi thanh cong cua cong thanh toan (Trieu: Transaction.process). */
    public static final String GATEWAY_SUCCESS_CODE = "00";

    private AppConfig() {
    }
}

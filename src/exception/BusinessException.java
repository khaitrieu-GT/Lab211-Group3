package exception;

/**
 * Loi nghiep vu: tang nghiep vu nem ra, controller bat va nho View hien thong bao cho nguoi dung.
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}

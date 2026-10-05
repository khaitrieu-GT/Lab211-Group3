package exception;

/**
 * Loi nghiep vu: controller nem ra, view bat va hien thong bao cho nguoi dung.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}

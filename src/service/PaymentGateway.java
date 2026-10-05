package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import model.Payment;
import model.Transaction;
import model.enums.PaymentMethod;
import util.AppConfig;

/**
 * Cong thanh toan mo phong (VNPAY/MOMO/...). Ma "00" la thanh cong, "51" la that bai.
 */
public class PaymentGateway {

    public static final String FAILED_CODE = "51";

    private static final DateTimeFormatter NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public Transaction charge(Payment payment, boolean approved) {
        String gateway = payment.getMethod() == PaymentMethod.CASH ? "COUNTER" : payment.getMethod().name();
        String transactionNo = gateway.charAt(0) + LocalDateTime.now().format(NO_FORMAT)
                + ThreadLocalRandom.current().nextInt(1000, 10000);
        String responseCode = approved ? AppConfig.GATEWAY_SUCCESS_CODE : FAILED_CODE;
        return new Transaction(null, payment.getId(), gateway, transactionNo, responseCode);
    }
}

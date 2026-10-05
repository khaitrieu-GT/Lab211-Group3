package repository;

import java.util.ArrayList;
import java.util.List;
import model.Transaction;

public class TransactionRepository extends CsvRepository<Transaction> {

    public TransactionRepository() {
        super("transactions.csv", "id,paymentId,gateway,transactionNo,status,responseCode,createdAt", "TX", 4);
    }

    @Override
    protected Transaction parse(String line) {
        return Transaction.fromCsvLine(line);
    }

    public List<Transaction> findByPaymentId(String paymentId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : findAll()) {
            if (t.getPaymentId().equalsIgnoreCase(paymentId)) {
                result.add(t);
            }
        }
        return result;
    }
}

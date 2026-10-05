package repository;

import java.util.ArrayList;
import java.util.List;
import model.CancellationRequest;

public class CancellationRequestRepository extends CsvRepository<CancellationRequest> {

    public CancellationRequestRepository() {
        super("cancellation_requests.csv", "id,eTicketId,userId,reason,requestDate,status,handledBy,note", "CR", 4);
    }

    @Override
    protected CancellationRequest parse(String line) {
        return CancellationRequest.fromCsvLine(line);
    }

    public List<CancellationRequest> findByUserId(String userId) {
        List<CancellationRequest> result = new ArrayList<>();
        for (CancellationRequest r : findAll()) {
            if (r.getUserId().equalsIgnoreCase(userId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<CancellationRequest> findPending() {
        List<CancellationRequest> result = new ArrayList<>();
        for (CancellationRequest r : findAll()) {
            if (r.isPending()) {
                result.add(r);
            }
        }
        return result;
    }

    public CancellationRequest findPendingByETicket(String eTicketId) {
        for (CancellationRequest r : findPending()) {
            if (r.getETicketId().equalsIgnoreCase(eTicketId)) {
                return r;
            }
        }
        return null;
    }
}

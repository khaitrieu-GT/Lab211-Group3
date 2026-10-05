package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.AuditLog;
import repository.AuditLogRepository;

/** Ghi nhat ky he thong (Minh: AuditLog.logAction). */
public class AuditService {

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    private final AuditLogRepository auditRepo = new AuditLogRepository();

    public void log(String actorUsername, String action, String statusResult) {
        AuditLog log = new AuditLog(null, actorUsername == null ? "system" : actorUsername, action, null, statusResult);
        log.logAction();
        this.auditRepo.insert(log);
    }

    /** Nhat ky moi nhat len dau. */
    public List<AuditLog> findAll() {
        List<AuditLog> list = new ArrayList<>(this.auditRepo.findAll());
        Collections.reverse(list);
        return list;
    }
}

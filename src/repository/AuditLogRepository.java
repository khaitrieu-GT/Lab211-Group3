package repository;

import model.AuditLog;

public class AuditLogRepository extends CsvRepository<AuditLog> {

    public AuditLogRepository() {
        super("audit_logs.csv", "id,actorUsername,action,timestamp,statusResult", "LOG", 5);
    }

    @Override
    public AuditLog parse(String line) {
        return AuditLog.fromCsvLine(line);
    }
}

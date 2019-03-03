package ricardo.monitoring.dbaudit;

@FunctionalInterface
public interface Auditor {

    void audit(AuditedOp op);
}

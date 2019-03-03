package ricardo.monitoring.dbaudit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log all {@link AuditedOp}.
 */
public class LoggingAuditor implements Auditor {

    private final static Logger LOGGER = LoggerFactory.getLogger(LoggingAuditor.class);

    @Override
    public void audit(AuditedOp op) {
        LOGGER.info("Audit: {}", op);
    }
}

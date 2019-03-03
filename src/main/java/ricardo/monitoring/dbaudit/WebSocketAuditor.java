package ricardo.monitoring.dbaudit;

import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Log all {@link AuditedOp}.
 */
public class WebSocketAuditor implements Auditor {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketAuditor(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void audit(AuditedOp op) {
        simpMessagingTemplate.convertAndSend("/topic/dbevents", op);
    }
}

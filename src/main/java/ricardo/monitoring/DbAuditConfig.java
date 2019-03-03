package ricardo.monitoring;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ricardo.monitoring.dbaudit.Auditor;
import ricardo.monitoring.dbaudit.WebSocketAuditor;
import ricardo.monitoring.dbaudit.hibernate.DbAuditCustomizer;

@Configuration
public class DbAuditConfig {

    @Bean
    public HibernatePropertiesCustomizer auditDbActions(Auditor auditor) {
        return new DbAuditCustomizer(auditor);
    }

    @Bean
    public Auditor websocketAuditor(SimpMessagingTemplate simpMessagingTemplate) {
        return new WebSocketAuditor(simpMessagingTemplate);
    }
}

package ricardo.monitoring.dbaudit.hibernate;

import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import ricardo.monitoring.dbaudit.Auditor;

import java.util.List;
import java.util.Map;

public class DbAuditCustomizer implements HibernatePropertiesCustomizer {

    private final Auditor auditor;

    public DbAuditCustomizer(Auditor auditor) {
        this.auditor = auditor;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.integrator_provider",
                (IntegratorProvider) () -> List.of(new DbAuditIntegrator(auditor)));
    }
}

package ricardo.monitoring.dbaudit.hibernate;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import ricardo.monitoring.dbaudit.Auditor;

class DbAuditIntegrator implements Integrator {

    private final Auditor auditor;

    DbAuditIntegrator(Auditor auditor) {
        this.auditor = auditor;
    }

    @Override
    public void integrate(
            Metadata metadata,
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {

        var eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);

        var listener = new DbAuditListener(auditor);
        eventListenerRegistry.appendListeners(EventType.POST_INSERT, listener);
        eventListenerRegistry.appendListeners(EventType.POST_UPDATE, listener);
        eventListenerRegistry.appendListeners(EventType.POST_DELETE, listener);
    }

    @Override
    public void disintegrate(
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {

    }
}

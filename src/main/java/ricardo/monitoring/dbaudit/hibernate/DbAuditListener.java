package ricardo.monitoring.dbaudit.hibernate;

import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import ricardo.monitoring.dbaudit.AuditedOp;
import ricardo.monitoring.dbaudit.Auditor;
import ricardo.monitoring.dbaudit.DbOp;

import java.util.StringJoiner;

class DbAuditListener
        implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private final Auditor auditAction;

    DbAuditListener(Auditor auditAction) {
        this.auditAction = auditAction;
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        var id = event.getId().toString();
        auditAction.audit(new AuditedOp(DbOp.INSERT, id));
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        var auditedOp = new AuditedOp(DbOp.DELETE, event.getId().toString());
        auditAction.audit(auditedOp);

    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        var auditedOp = new AuditedOp(DbOp.UPDATE, event.getId().toString());
        auditAction.audit(auditedOp);
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}

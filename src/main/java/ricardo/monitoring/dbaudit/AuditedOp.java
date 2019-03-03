package ricardo.monitoring.dbaudit;

import java.util.StringJoiner;

public final class AuditedOp {

    private final DbOp op;
    private final String id;

    public AuditedOp(DbOp op, String id) {
        this.op = op;
        this.id = id;
    }

    public DbOp getOp() {
        return op;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AuditedOp.class.getSimpleName() + "[", "]")
                .add("op=" + op)
                .add("id='" + id + "'")
                .toString();
    }
}

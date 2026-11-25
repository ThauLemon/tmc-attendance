package com.tmc.attendance.audit;

import com.tmc.attendance.contracts.AuditRecord;
import com.tmc.attendance.contracts.IAuditWriter;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Ghi audit_log bằng JDBC. Lớp này chỉ có INSERT: không có update hay delete (ISP, chỉ ghi thêm).
 * Service nào cần audit thì tự tạo bean: {@code new JdbcAuditWriter(jdbcTemplate)}.
 *
 * <p>Bảng cần có (xem audit-schema.sql). TODO Anh: RBAC tối thiểu, quyền DB chỉ cho INSERT.
 */
public class JdbcAuditWriter implements IAuditWriter {

    private static final String INSERT_SQL =
            "INSERT INTO audit_log (occurred_at, actor, action, target, detail) VALUES (?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbc;

    public JdbcAuditWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void write(AuditRecord record) {
        jdbc.update(INSERT_SQL,
                Timestamp.from(record.occurredAt()),
                record.actor(),
                record.action(),
                record.target(),
                record.detail());
    }
}

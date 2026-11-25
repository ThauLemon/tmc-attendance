package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.AttendanceStatus;
import com.tmc.attendance.contracts.Direction;
import com.tmc.attendance.contracts.IAttendanceStore;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Kho SQLite tại kiosk. Dùng JdbcTemplate, không dùng Hibernate.
 * Pool chỉ có một connection (application.yml) nên luồng quét và sync worker không ghi chồng nhau.
 */
@Repository
public class SqliteAttendanceStore implements IAttendanceStore {

    private final JdbcTemplate jdbc;

    public SqliteAttendanceStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void saveBeforeOpen(AttendanceEvent e) {
        jdbc.update("""
                INSERT INTO attendance_event
                    (transaction_id, employee_id, device_id, kiosk_id, site_id, direction, scanned_at, status, nonce, synced)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """,
                e.transactionId(), e.employeeId(), e.deviceId(), e.kioskId(), e.siteId(), e.direction().name(),
                e.scannedAt().toString(), e.status().name(), e.nonce());
    }

    @Override
    public void updateStatus(String transactionId, AttendanceStatus status) {
        jdbc.update("UPDATE attendance_event SET status = ? WHERE transaction_id = ?", status.name(), transactionId);
    }

    @Override
    public List<AttendanceEvent> findByStatus(AttendanceStatus status) {
        return jdbc.query("SELECT * FROM attendance_event WHERE status = ? ORDER BY scanned_at",
                SqliteAttendanceStore::mapRow, status.name());
    }

    @Override
    public List<AttendanceEvent> findUnsynced(int limit) {
        return jdbc.query("""
                SELECT * FROM attendance_event
                WHERE synced = 0 AND status <> 'PENDING'
                ORDER BY scanned_at
                LIMIT ?
                """, SqliteAttendanceStore::mapRow, limit);
    }

    @Override
    public void markSynced(String transactionId) {
        jdbc.update("UPDATE attendance_event SET synced = 1 WHERE transaction_id = ?", transactionId);
    }

    private static AttendanceEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AttendanceEvent(
                rs.getString("transaction_id"),
                rs.getString("employee_id"),
                rs.getString("device_id"),
                rs.getString("kiosk_id"),
                rs.getString("site_id"),
                Direction.valueOf(rs.getString("direction")),
                Instant.parse(rs.getString("scanned_at")),
                AttendanceStatus.valueOf(rs.getString("status")),
                rs.getString("nonce"));
    }
}

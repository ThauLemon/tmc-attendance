package com.tmc.attendance.service;

import com.tmc.attendance.contracts.AttendanceEvent;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AttendanceRecordRepository {

    private final JdbcTemplate jdbc;

    public AttendanceRecordRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Ghi idempotent theo transaction_id: message gửi lặp (kiosk gửi lại sau khi mất ack) không tạo bản ghi trùng.
     *
     * @return true nếu là bản ghi mới, false nếu đã có
     */
    public boolean insertIfAbsent(AttendanceEvent e) {
        int inserted = jdbc.update("""
                INSERT INTO attendance_record
                    (transaction_id, employee_id, device_id, kiosk_id, site_id, direction, scanned_at, status, nonce)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (transaction_id) DO NOTHING
                """,
                e.transactionId(), e.employeeId(), e.deviceId(), e.kioskId(), e.siteId(), e.direction().name(),
                Timestamp.from(e.scannedAt()), e.status().name(), e.nonce());
        return inserted == 1;
    }

    public List<Map<String, Object>> findByEmployee(String employeeId) {
        return jdbc.queryForList("""
                SELECT transaction_id, employee_id, kiosk_id, site_id, direction, scanned_at, status, received_at
                FROM attendance_record
                WHERE employee_id = ?
                ORDER BY scanned_at DESC
                LIMIT 100
                """, employeeId);
    }

    public long count() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM attendance_record", Long.class);
        return total == null ? 0 : total;
    }
}

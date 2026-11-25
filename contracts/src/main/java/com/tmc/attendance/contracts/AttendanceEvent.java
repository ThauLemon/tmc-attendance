package com.tmc.attendance.contracts;

import java.time.Instant;

/**
 * Một lượt ra vào. Vừa là bản ghi trong SQLite của kiosk, vừa là nội dung message gửi qua RabbitMQ.
 * transactionId là khóa khử trùng ở mọi tầng (ADR-04).
 */
public record AttendanceEvent(
        String transactionId,
        String employeeId,
        String deviceId,
        String kioskId,
        String siteId,
        Direction direction,
        Instant scannedAt,
        AttendanceStatus status,
        String nonce) {

    public AttendanceEvent withStatus(AttendanceStatus newStatus) {
        return new AttendanceEvent(transactionId, employeeId, deviceId, kioskId, siteId, direction, scannedAt,
                newStatus, nonce);
    }
}

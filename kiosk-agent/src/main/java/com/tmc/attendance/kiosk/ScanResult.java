package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.AttendanceStatus;

/** Kết quả một lượt quét, hiển thị trên màn hình kiosk. latencyMs dùng để chứng minh NFR-01. */
public record ScanResult(
        String transactionId,
        AttendanceStatus status,
        boolean gateOpened,
        String reason,
        long latencyMs) {

    public static ScanResult rejected(String reason, long latencyMs) {
        return new ScanResult(null, null, false, reason, latencyMs);
    }
}

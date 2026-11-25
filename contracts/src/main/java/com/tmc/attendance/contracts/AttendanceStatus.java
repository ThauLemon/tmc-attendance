package com.tmc.attendance.contracts;

/** Trạng thái một lượt ra vào tại kiosk. */
public enum AttendanceStatus {
    /** Đã ghi xuống SQLite, chưa mở cổng (write-before-open). */
    PENDING,
    /** Cổng đã mở. */
    CONFIRMED,
    /** Turnstile timeout hoặc kiosk sập giữa giao dịch: cần bảo vệ đối soát (ADR-10). */
    PENDING_VERIFICATION,
    /** Cổng báo lỗi, không mở. */
    FAILED
}

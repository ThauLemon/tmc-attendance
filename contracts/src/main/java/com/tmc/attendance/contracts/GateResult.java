package com.tmc.attendance.contracts;

/** Kết quả lệnh mở cổng gửi tới turnstile. */
public enum GateResult {
    /** Turnstile xác nhận đã mở. */
    OPENED,
    /** Không nhận được phản hồi trong thời gian chờ: chưa biết cổng có mở hay không (ADR-10). */
    TIMEOUT,
    /** Turnstile báo lỗi, chắc chắn không mở. */
    FAULT
}

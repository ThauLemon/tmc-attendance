package com.tmc.attendance.contracts;

import java.time.Instant;

/**
 * Kiểm tra QR token tại kiosk: chữ ký thiết bị, TTL, nonce dùng một lần, anti-passback.
 * Cài đặt thật: Hưng (kiosk-agent). Skeleton đang dùng StubQrValidator.
 */
public interface IQrValidator {

    ValidationResult validate(String rawToken, Direction direction, Instant now);
}

package com.tmc.attendance.contracts;

/**
 * Khóa công khai của một thiết bị đã đăng ký. identity-service đẩy danh sách này xuống kiosk (ADR-08).
 *
 * @param publicKeySpkiBase64 khóa công khai EC P-256 dạng SPKI (WebCrypto exportKey("spki")),
 *                            Java đọc bằng X509EncodedKeySpec.
 */
public record DeviceKey(String deviceId, String employeeId, String publicKeySpkiBase64, boolean revoked) {
}

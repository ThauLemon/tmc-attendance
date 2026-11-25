package com.tmc.attendance.contracts;

/** Kết quả kiểm tra một QR token tại kiosk. */
public record ValidationResult(
        boolean valid,
        String employeeId,
        String deviceId,
        String nonce,
        String reason) {

    public static ValidationResult accept(String employeeId, String deviceId, String nonce) {
        return new ValidationResult(true, employeeId, deviceId, nonce, null);
    }

    public static ValidationResult reject(String reason) {
        return new ValidationResult(false, null, null, null, reason);
    }
}

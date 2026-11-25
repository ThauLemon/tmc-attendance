package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.Direction;
import com.tmc.attendance.contracts.IQrValidator;
import com.tmc.attendance.contracts.ValidationResult;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * SKELETON: chấp nhận mọi token dạng {@code EMP:<mã nhân viên>}, chưa kiểm chữ ký.
 *
 * <p>TODO Hưng: thay lớp này bằng validator thật (chữ ký thiết bị qua QrTokenSigner, TTL 15 giây cộng sai số,
 * nonce dùng một lần, anti-passback). Khi có lớp mới cài đặt IQrValidator thì xóa lớp này.
 */
@Component
public class StubQrValidator implements IQrValidator {

    private static final String PREFIX = "EMP:";

    @Override
    public ValidationResult validate(String rawToken, Direction direction, Instant now) {
        if (rawToken == null || !rawToken.startsWith(PREFIX) || rawToken.length() == PREFIX.length()) {
            return ValidationResult.reject("Token không hợp lệ (skeleton chỉ nhận dạng EMP:<mã>)");
        }
        String employeeId = rawToken.substring(PREFIX.length()).trim();
        return ValidationResult.accept(employeeId, "stub-device", UUID.randomUUID().toString());
    }
}

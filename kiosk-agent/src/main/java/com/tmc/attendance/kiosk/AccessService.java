package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.AttendanceStatus;
import com.tmc.attendance.contracts.Direction;
import com.tmc.attendance.contracts.GateResult;
import com.tmc.attendance.contracts.IAttendanceStore;
import com.tmc.attendance.contracts.IGateController;
import com.tmc.attendance.contracts.IQrValidator;
import com.tmc.attendance.contracts.ValidationResult;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Luồng xử lý một lượt quét tại kiosk. Chỉ phụ thuộc interface trong contracts/ (DIP),
 * nên đổi validator, cổng hay kho lưu trữ không phải sửa lớp này.
 *
 * <ol>
 *   <li>Kiểm QR.</li>
 *   <li>Ghi PENDING xuống SQLite trước khi mở cổng (write-before-open).</li>
 *   <li>Mở cổng, cập nhật trạng thái cuối theo phản hồi của turnstile.</li>
 * </ol>
 *
 * Việc gửi lên trung tâm do AttendanceSyncWorker làm ở nền, nên mất WAN không làm chậm cổng.
 */
@Service
public class AccessService {

    private final IQrValidator validator;
    private final IGateController gate;
    private final IAttendanceStore store;
    private final KioskProperties properties;
    private final Clock clock;

    public AccessService(IQrValidator validator, IGateController gate, IAttendanceStore store,
                         KioskProperties properties, Clock clock) {
        this.validator = validator;
        this.gate = gate;
        this.store = store;
        this.properties = properties;
        this.clock = clock;
    }

    public ScanResult scan(String rawToken, Direction direction) {
        long startNanos = System.nanoTime();
        Instant now = clock.instant();

        ValidationResult validation = validator.validate(rawToken, direction, now);
        if (!validation.valid()) {
            return ScanResult.rejected(validation.reason(), elapsedMillis(startNanos));
        }

        String transactionId = UUID.randomUUID().toString();
        AttendanceEvent event = new AttendanceEvent(transactionId, validation.employeeId(), validation.deviceId(),
                properties.kioskId(), properties.siteId(), direction, now, AttendanceStatus.PENDING,
                validation.nonce());
        store.saveBeforeOpen(event);

        GateResult gateResult = gate.open(transactionId, direction);
        AttendanceStatus finalStatus = switch (gateResult) {
            case OPENED -> AttendanceStatus.CONFIRMED;
            case TIMEOUT -> AttendanceStatus.PENDING_VERIFICATION;
            case FAULT -> AttendanceStatus.FAILED;
        };
        store.updateStatus(transactionId, finalStatus);

        return new ScanResult(transactionId, finalStatus, gateResult == GateResult.OPENED, null,
                elapsedMillis(startNanos));
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}

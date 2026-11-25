package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.AttendanceStatus;
import com.tmc.attendance.contracts.IAttendanceStore;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Khi kiosk khởi động lại, bản ghi còn PENDING nghĩa là kiosk sập giữa giao dịch:
 * không biết cổng đã mở hay chưa, nên chuyển sang PENDING_VERIFICATION để bảo vệ đối soát.
 *
 * <p>TODO Nam: hỏi turnstile theo transactionId trước khi kết luận (ADR-10).
 */
@Component
public class PendingTransactionRecovery implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PendingTransactionRecovery.class);

    private final IAttendanceStore store;

    public PendingTransactionRecovery(IAttendanceStore store) {
        this.store = store;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<AttendanceEvent> pending = store.findByStatus(AttendanceStatus.PENDING);
        for (AttendanceEvent event : pending) {
            store.updateStatus(event.transactionId(), AttendanceStatus.PENDING_VERIFICATION);
        }
        if (!pending.isEmpty()) {
            log.warn("Phục hồi {} giao dịch dở dang, chuyển sang PENDING_VERIFICATION", pending.size());
        }
    }
}

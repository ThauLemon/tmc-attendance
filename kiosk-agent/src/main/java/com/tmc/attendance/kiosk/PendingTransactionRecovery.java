package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.AttendanceStatus;
import com.tmc.attendance.contracts.GateResult;
import com.tmc.attendance.contracts.IGateController;
import com.tmc.attendance.contracts.IAttendanceStore;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Đối soát giao dịch PENDING và PENDING_VERIFICATION khi khởi động và định kỳ bằng transactionId.
 *
 */
@Component
public class PendingTransactionRecovery implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PendingTransactionRecovery.class);

    private final IAttendanceStore store;
    private final IGateController gate;

    public PendingTransactionRecovery(IAttendanceStore store, IGateController gate) {
        this.store = store;
        this.gate = gate;
    }

    @Override
    public void run(ApplicationArguments args) {
        reconcilePendingTransactions();
    }

    @Scheduled(fixedDelayString = "${kiosk.recovery-interval-ms:5000}")
    public void reconcilePendingTransactions() {
        List<AttendanceEvent> transactions = new ArrayList<>(store.findByStatus(AttendanceStatus.PENDING));
        transactions.addAll(store.findByStatus(AttendanceStatus.PENDING_VERIFICATION));

        int confirmed = 0;
        int failed = 0;
        int unresolved = 0;
        for (AttendanceEvent event : transactions) {
            AttendanceStatus status = switch (gate.getStatus(event.transactionId())) {
                case OPENED -> AttendanceStatus.CONFIRMED;
                case FAULT -> AttendanceStatus.FAILED;
                case TIMEOUT -> AttendanceStatus.PENDING_VERIFICATION;
            };
            if (event.status() != status) {
                store.updateStatus(event.transactionId(), status);
            }
            switch (status) {
                case CONFIRMED -> confirmed++;
                case FAILED -> failed++;
                case PENDING_VERIFICATION -> unresolved++;
                default -> throw new IllegalStateException("Unexpected recovery status: " + status);
            }
        }
        if (confirmed + failed > 0) {
            log.info("Đối soát {} giao dịch: {} xác nhận, {} lỗi, {} chưa rõ",
                    transactions.size(), confirmed, failed, unresolved);
        } else if (unresolved > 0) {
            log.debug("Chưa xác định được trạng thái của {} giao dịch", unresolved);
        }
    }
}

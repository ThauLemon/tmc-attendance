package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.IAttendancePublisher;
import com.tmc.attendance.contracts.IAttendanceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Store-and-forward (ADR-04): định kỳ đẩy bản ghi chưa đồng bộ lên RabbitMQ.
 * Chỉ đánh dấu đã đồng bộ khi broker xác nhận. Mất WAN thì dừng lượt này, lượt sau thử lại.
 *
 * <p>TODO Dũng: backoff khi mất kết nối, metric số bản ghi tồn.
 */
@Component
public class AttendanceSyncWorker {

    private static final Logger log = LoggerFactory.getLogger(AttendanceSyncWorker.class);
    private static final int BATCH_SIZE = 100;

    private final IAttendanceStore store;
    private final IAttendancePublisher publisher;

    public AttendanceSyncWorker(IAttendanceStore store, IAttendancePublisher publisher) {
        this.store = store;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${kiosk.sync-interval-ms:2000}")
    public void syncBatch() {
        int synced = 0;
        for (AttendanceEvent event : store.findUnsynced(BATCH_SIZE)) {
            if (!publisher.publish(event)) {
                log.debug("Chưa gửi được {}, sẽ thử lại lượt sau", event.transactionId());
                break;
            }
            store.markSynced(event.transactionId());
            synced++;
        }
        if (synced > 0) {
            log.info("Đã đồng bộ {} bản ghi lên trung tâm", synced);
        }
    }
}

package com.tmc.attendance.service;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.AttendanceMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Idempotent consumer: nhận AttendanceEvent từ kiosk và ghi PostgreSQL.
 *
 * <p>TODO Dũng: gắn cờ QR dùng lặp (cùng nonce xuất hiện ở hai kiosk khi mất WAN, đánh đổi ở ADR-09).
 */
@Component
public class AttendanceEventListener {

    private static final Logger log = LoggerFactory.getLogger(AttendanceEventListener.class);

    private final AttendanceRecordRepository repository;

    public AttendanceEventListener(AttendanceRecordRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = AttendanceMessaging.QUEUE)
    public void onAttendanceRecorded(AttendanceEvent event) {
        if (repository.insertIfAbsent(event)) {
            log.info("Ghi nhận {} {} tại {}", event.employeeId(), event.direction(), event.kioskId());
        } else {
            log.info("Bỏ qua bản ghi trùng {}", event.transactionId());
        }
    }
}

package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.AttendanceMessaging;
import com.tmc.attendance.contracts.IAttendancePublisher;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Gửi lên RabbitMQ với publisher confirms (spring.rabbitmq.publisher-confirm-type=correlated).
 * Chỉ trả true khi broker ack và message không bị trả về vì không định tuyến được.
 */
@Component
public class RabbitAttendancePublisher implements IAttendancePublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitAttendancePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final KioskProperties properties;

    public RabbitAttendancePublisher(RabbitTemplate rabbitTemplate, KioskProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public boolean publish(AttendanceEvent event) {
        CorrelationData correlation = new CorrelationData(event.transactionId());
        try {
            rabbitTemplate.convertAndSend(AttendanceMessaging.EXCHANGE, AttendanceMessaging.ROUTING_KEY, event,
                    correlation);
            CorrelationData.Confirm confirm = correlation.getFuture()
                    .get(properties.confirmTimeout().toMillis(), TimeUnit.MILLISECONDS);
            return confirm.isAck() && correlation.getReturned() == null;
        } catch (AmqpException | ExecutionException | TimeoutException e) {
            log.debug("Không gửi được {}: {}", event.transactionId(), e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

package com.tmc.attendance.kiosk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.attendance.contracts.AttendanceMessaging;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange attendanceExchange() {
        return new TopicExchange(AttendanceMessaging.EXCHANGE, true, false);
    }

    /** Gửi AttendanceEvent dạng JSON, dùng ObjectMapper của Spring Boot (đã hỗ trợ Instant). */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

package com.tmc.attendance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.attendance.contracts.AttendanceMessaging;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exchange, queue và dead-letter queue. Message lỗi không bị requeue mãi
 * (spring.rabbitmq.listener.simple.default-requeue-rejected=false) mà sang DLQ.
 */
@Configuration
public class RabbitTopologyConfig {

    @Bean
    TopicExchange attendanceExchange() {
        return new TopicExchange(AttendanceMessaging.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(AttendanceMessaging.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue attendanceQueue() {
        return QueueBuilder.durable(AttendanceMessaging.QUEUE)
                .deadLetterExchange(AttendanceMessaging.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(AttendanceMessaging.DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(AttendanceMessaging.DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding attendanceBinding(Queue attendanceQueue, TopicExchange attendanceExchange) {
        return BindingBuilder.bind(attendanceQueue).to(attendanceExchange).with(AttendanceMessaging.ROUTING_KEY);
    }

    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(AttendanceMessaging.DEAD_LETTER_QUEUE);
    }

    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

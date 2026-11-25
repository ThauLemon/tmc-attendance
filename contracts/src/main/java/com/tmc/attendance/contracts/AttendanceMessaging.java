package com.tmc.attendance.contracts;

/** Tên exchange, queue và routing key dùng chung giữa kiosk-agent và attendance-service. */
public final class AttendanceMessaging {

    public static final String EXCHANGE = "tmc.attendance";
    public static final String ROUTING_KEY = "attendance.recorded";
    public static final String QUEUE = "attendance.recorded";

    public static final String DEAD_LETTER_EXCHANGE = "tmc.attendance.dlx";
    public static final String DEAD_LETTER_QUEUE = "attendance.recorded.dlq";

    private AttendanceMessaging() {
    }
}

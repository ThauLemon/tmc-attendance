package com.tmc.attendance.contracts;

/** Gửi một lượt ra vào từ kiosk lên trung tâm. Cài đặt: Dũng. */
public interface IAttendancePublisher {

    /**
     * @return true chỉ khi broker đã xác nhận lưu message (publisher confirm ack).
     *         Trả false khi mất kết nối, bị nack hoặc message không định tuyến được.
     */
    boolean publish(AttendanceEvent event);
}
